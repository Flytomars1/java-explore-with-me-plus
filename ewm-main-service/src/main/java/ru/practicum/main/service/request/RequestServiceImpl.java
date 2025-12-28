package ru.practicum.main.service.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.main.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.main.dto.request.ParticipationRequestDto;
import ru.practicum.main.exception.ConflictException;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.mapper.RequestMapper;
import ru.practicum.main.model.Event;
import ru.practicum.main.model.EventState;
import ru.practicum.main.model.ParticipationRequest;
import ru.practicum.main.model.RequestStatus;
import ru.practicum.main.model.User;
import ru.practicum.main.repository.EventRepository;
import ru.practicum.main.repository.RequestRepository;
import ru.practicum.main.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RequestServiceImpl implements RequestService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        ensureUserExists(userId);
        return requestRepository.findAllByRequester_IdOrderByCreatedDesc(userId)
                .stream()
                .map(RequestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ParticipationRequestDto addParticipationRequest(Long userId, Long eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (event.getInitiator() != null && Objects.equals(event.getInitiator().getId(), userId)) {
            throw new ConflictException("Initiator cannot request participation in own event");
        }

        if (event.getState() == null || event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Cannot participate in unpublished event");
        }

        if (requestRepository.existsByRequester_IdAndEvent_Id(userId, eventId)) {
            throw new ConflictException("Duplicate request");
        }

        int limit = safeInt(event.getParticipantLimit(), 0);
        // По умолчанию в EWM модерация включена (true). Null трактуем как true.
        boolean moderation = !Boolean.FALSE.equals(event.getRequestModeration());

        long confirmedCount = requestRepository.countByEvent_IdAndStatus(eventId, RequestStatus.CONFIRMED);
        if (limit > 0 && confirmedCount >= limit) {
            throw new ConflictException("The participant limit has been reached");
        }

        RequestStatus status;
        if (limit == 0 || !moderation) {
            status = RequestStatus.CONFIRMED;
        } else {
            status = RequestStatus.PENDING;
        }

        ParticipationRequest request = ParticipationRequest.builder()
                .event(event)
                .requester(user)
                .status(status)
                .created(LocalDateTime.now())
                .build();

        ParticipationRequest saved = requestRepository.save(request);
        log.info("Created participation request id={}, userId={}, eventId={}, status={}",
                saved.getId(), userId, eventId, saved.getStatus());
        return RequestMapper.toDto(saved);
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        ensureUserExists(userId);

        ParticipationRequest request = requestRepository.findByIdAndRequester_Id(requestId, userId)
                .orElseThrow(() -> new NotFoundException("Request with id=" + requestId + " was not found"));

        request.setStatus(RequestStatus.CANCELED);
        ParticipationRequest saved = requestRepository.save(request);
        return RequestMapper.toDto(saved);
    }

    @Override
    public List<ParticipationRequestDto> getEventParticipants(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (event.getInitiator() == null || !Objects.equals(event.getInitiator().getId(), userId)) {
            // "не найдено или недоступно" — прячем подробности
            throw new NotFoundException("Event with id=" + eventId + " was not found");
        }

        return requestRepository.findAllByEvent_IdOrderByCreatedAsc(eventId)
                .stream()
                .map(RequestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult changeRequestStatus(
            Long userId,
            Long eventId,
            EventRequestStatusUpdateRequest updateRequest
    ) {
        if (updateRequest == null || updateRequest.getRequestIds() == null) {
            throw new IllegalArgumentException("requestIds must not be null");
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (event.getInitiator() == null || !Objects.equals(event.getInitiator().getId(), userId)) {
            throw new NotFoundException("Event with id=" + eventId + " was not found");
        }

        String desired = updateRequest.getStatus();
        if (desired == null || (!desired.equals("CONFIRMED") && !desired.equals("REJECTED"))) {
            throw new IllegalArgumentException("Status must be CONFIRMED or REJECTED");
        }

        List<ParticipationRequest> requests =
                requestRepository.findAllByIdInAndEvent_Id(updateRequest.getRequestIds(), eventId);

        if (requests.size() != updateRequest.getRequestIds().size()) {
            // часть заявок не найдена/не принадлежит событию
            throw new NotFoundException("Request was not found");
        }

        // все заявки должны быть PENDING
        for (ParticipationRequest req : requests) {
            if (req.getStatus() != RequestStatus.PENDING) {
                throw new ConflictException("Request must have status PENDING");
            }
        }

        int limit = safeInt(event.getParticipantLimit(), 0);
        long confirmedCount = requestRepository.countByEvent_IdAndStatus(eventId, RequestStatus.CONFIRMED);

        List<ParticipationRequestDto> confirmedDtos = new ArrayList<>();
        List<ParticipationRequestDto> rejectedDtos = new ArrayList<>();

        if (desired.equals("REJECTED")) {
            for (ParticipationRequest req : requests) {
                req.setStatus(RequestStatus.REJECTED);
                rejectedDtos.add(RequestMapper.toDto(req));
            }
            requestRepository.saveAll(requests);
            return EventRequestStatusUpdateResult.builder()
                    .confirmedRequests(Collections.emptyList())
                    .rejectedRequests(rejectedDtos)
                    .build();
        }

        // desired CONFIRMED
        for (ParticipationRequest req : requests) {
            if (limit > 0 && confirmedCount >= limit) {
                // лимит достигнут: оставшиеся отклоняем
                req.setStatus(RequestStatus.REJECTED);
                rejectedDtos.add(RequestMapper.toDto(req));
                continue;
            }
            req.setStatus(RequestStatus.CONFIRMED);
            confirmedCount++;
            confirmedDtos.add(RequestMapper.toDto(req));
        }

        // если лимит исчерпан — все оставшиеся PENDING заявки события нужно отклонить
        if (limit > 0 && confirmedCount >= limit) {
            List<ParticipationRequest> pendingToReject = requestRepository.findAllByEvent_IdOrderByCreatedAsc(eventId)
                    .stream()
                    .filter(r -> r.getStatus() == RequestStatus.PENDING)
                    .collect(Collectors.toList());

            for (ParticipationRequest req : pendingToReject) {
                req.setStatus(RequestStatus.REJECTED);
                rejectedDtos.add(RequestMapper.toDto(req));
            }
            requestRepository.saveAll(pendingToReject);
        }

        requestRepository.saveAll(requests);

        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(confirmedDtos)
                .rejectedRequests(rejectedDtos)
                .build();
    }

    private void ensureUserExists(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " was not found");
        }
    }

    private int safeInt(Integer value, int defaultValue) {
        return value == null ? defaultValue : value;
    }
}
