package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.*;
import ru.practicum.ewm.mapper.EventMapper;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.EventState;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.stats.client.StatsClient;
import ru.practicum.stats.dto.HitDto;
import ru.practicum.stats.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventRepository repository;
    private final StatsClient statsClient;

    @Override
    @Transactional
    public EventFullDto create(Long userId, NewEventDto dto) {
        Event e = EventMapper.toEntity(dto, userId);
        return EventMapper.toFull(repository.save(e), 0);
    }

    @Override
    @Transactional
    public EventFullDto updateByUser(Long userId, Long eventId, UpdateEventUserRequest dto) {
        Event e = repository.findById(eventId).orElseThrow(() -> new NoSuchElementException("Event not found"));
        if (!Objects.equals(e.getInitiatorId(), userId)) throw new IllegalStateException("Event belongs to another user");
        if (e.getState() == EventState.PUBLISHED) throw new IllegalStateException("Cannot edit published event");
        EventMapper.applyUserUpdate(e, dto);
        return EventMapper.toFull(repository.save(e), 0);
    }

    @Override
    public EventFullDto getUserEvent(Long userId, Long eventId) {
        Event e = repository.findById(eventId).orElseThrow(() -> new NoSuchElementException("Event not found"));
        if (!Objects.equals(e.getInitiatorId(), userId)) throw new IllegalStateException("Event belongs to another user");
        long views = fetchViews(e.getId(), e.getPublishedOn() != null ? e.getPublishedOn() : e.getCreatedOn(), LocalDateTime.now());
        return EventMapper.toFull(e, views);
    }

    @Override
    public Page<EventShortDto> getUserEvents(Long userId, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("createdOn").descending());
        Page<Event> page = repository.findAllByInitiatorId(userId, pageable);
        return page.map(e -> EventMapper.toShort(e, fetchViews(e.getId(),
                e.getPublishedOn() != null ? e.getPublishedOn() : e.getCreatedOn(), LocalDateTime.now())));
    }

    @Override
    public List<EventShortDto> searchPublic(String text, List<Long> categories, Boolean paid,
                                            LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                            Boolean onlyAvailable, String sort, int from, int size, String requestUri, String ip) {
        statsClient.hit(buildHit("ewm-main-service", requestUri, ip, LocalDateTime.now()));

        Specification<Event> spec = Specification.where(published())
                .and(betweenDates(rangeStart, rangeEnd))
                .and(categories == null || categories.isEmpty() ? null : inCategories(categories))
                .and(paid == null ? null : paidEq(paid))
                .and(text == null || text.isBlank() ? null : textLike(text));

        Sort s = "EVENT_DATE".equalsIgnoreCase(sort) ? Sort.by("eventDate").ascending()
                : Sort.by("eventDate").ascending();

        Pageable pageable = PageRequest.of(from / size, size, s);
        Page<Event> page = repository.findAll(spec, pageable);

        List<EventShortDto> result = new ArrayList<>(page.getNumberOfElements());
        for (Event e : page.getContent()) {
            long views = fetchViews(e.getId(), e.getPublishedOn() != null ? e.getPublishedOn() : e.getCreatedOn(), LocalDateTime.now());
            result.add(EventMapper.toShort(e, views));
        }
        return result;
    }

    @Override
    public EventFullDto getPublicById(Long eventId, String requestUri, String ip) {
        Event e = repository.findById(eventId).orElseThrow(() -> new NoSuchElementException("Event not found"));
        if (e.getState() != EventState.PUBLISHED) throw new NoSuchElementException("Event is not published");
        statsClient.hit(buildHit("ewm-main-service", requestUri, ip, LocalDateTime.now()));
        long views = fetchViews(e.getId(), e.getPublishedOn() != null ? e.getPublishedOn() : e.getCreatedOn(), LocalDateTime.now());
        return EventMapper.toFull(e, views);
    }

    @Override
    public List<EventFullDto> searchAdmin(List<Long> users, List<String> states, List<Long> categories,
                                          LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size) {

        Specification<Event> spec = Specification.where(betweenDates(rangeStart, rangeEnd))
                .and(users == null || users.isEmpty() ? null : initiatorsIn(users))
                .and(states == null || states.isEmpty() ? null : stateIn(states))
                .and(categories == null || categories.isEmpty() ? null : inCategories(categories));
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("createdOn").descending());
        Page<Event> page = repository.findAll(spec, pageable);

        List<EventFullDto> out = new ArrayList<>(page.getContent().size());
        for (Event e : page.getContent()) {
            long views = fetchViews(e.getId(), e.getPublishedOn() != null ? e.getPublishedOn() : e.getCreatedOn(), LocalDateTime.now());
            out.add(EventMapper.toFull(e, views));
        }
        return out;
    }

    @Override
    @Transactional
    public EventFullDto publish(Long eventId) {
        Event e = repository.findById(eventId).orElseThrow(() -> new NoSuchElementException("Event not found"));
        if (e.getState() != EventState.PENDING) throw new IllegalStateException("Only PENDING events can be published");
        e.setState(EventState.PUBLISHED);
        e.setPublishedOn(LocalDateTime.now());
        return EventMapper.toFull(repository.save(e), 0);
    }

    @Override
    @Transactional
    public EventFullDto reject(Long eventId) {
        Event e = repository.findById(eventId).orElseThrow(() -> new NoSuchElementException("Event not found"));
        if (e.getState() == EventState.PUBLISHED) throw new IllegalStateException("Published events cannot be rejected");
        e.setState(EventState.CANCELED);
        return EventMapper.toFull(repository.save(e), 0);
    }

    @Override
    @Transactional
    public EventFullDto updateByAdmin(Long eventId, UpdateEventAdminRequest dto) {
        Event e = repository.findById(eventId).orElseThrow(() -> new NoSuchElementException("Event not found"));
        EventMapper.applyAdminUpdate(e, dto);
        return EventMapper.toFull(repository.save(e), 0);
    }

    /* Helpers */

    private HitDto buildHit(String app, String uri, String ip, LocalDateTime ts) {
        HitDto hit = new HitDto();
        hit.setApp(app);
        hit.setUri(uri);
        hit.setIp(ip);
        hit.setTimestamp(ts);
        return hit;
    }

    private long fetchViews(Long eventId, LocalDateTime start, LocalDateTime end) {
        String uri = "/events/" + eventId;
        List<ViewStatsDto> stats = statsClient.getStats(start, end, Collections.singletonList(uri), false);
        return stats.stream().mapToLong(ViewStatsDto::getHits).sum();
    }

    /* Specifications */

    private Specification<Event> published() {
        return (root, q, cb) -> cb.equal(root.get("state"), EventState.PUBLISHED);
    }
    private Specification<Event> betweenDates(LocalDateTime start, LocalDateTime end) {
        return (root, q, cb) -> {
            if (start != null && end != null) return cb.between(root.get("eventDate"), start, end);
            if (start != null) return cb.greaterThanOrEqualTo(root.get("eventDate"), start);
            if (end != null) return cb.lessThanOrEqualTo(root.get("eventDate"), end);
            return cb.conjunction();
        };
    }
    private Specification<Event> inCategories(List<Long> categories) {
        return (root, q, cb) -> root.get("categoryId").in(categories);
    }
    private Specification<Event> paidEq(Boolean paid) {
        return (root, q, cb) -> cb.equal(root.get("paid"), paid);
    }
    private Specification<Event> textLike(String text) {
        String like = "%" + text.toLowerCase() + "%";
        return (root, q, cb) -> cb.or(
                cb.like(cb.lower(root.get("annotation")), like),
                cb.like(cb.lower(root.get("description")), like)
        );
    }
    private Specification<Event> initiatorsIn(List<Long> users) {
        return (root, q, cb) -> root.get("initiatorId").in(users);
    }
    private Specification<Event> stateIn(List<String> states) {
        List<EventState> es = states.stream().map(s -> EventState.valueOf(s.toUpperCase())).toList();
        return (root, q, cb) -> root.get("state").in(es);
    }
}