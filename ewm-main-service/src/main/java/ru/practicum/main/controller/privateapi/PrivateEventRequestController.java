package ru.practicum.main.controller.privateapi;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.main.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.main.dto.request.ParticipationRequestDto;
import ru.practicum.main.service.request.RequestService;

import java.util.List;


@RestController
@RequestMapping(path = "/users/{userId}/events/{eventId}/requests")
@RequiredArgsConstructor
public class PrivateEventRequestController {

    private final RequestService requestService;

    @GetMapping
    public List<ParticipationRequestDto> getEventParticipants(
            @PathVariable Long userId,
            @PathVariable Long eventId
    ) {
        return requestService.getEventParticipants(userId, eventId);
    }

    @PatchMapping
    public EventRequestStatusUpdateResult changeRequestStatus(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @RequestBody EventRequestStatusUpdateRequest updateRequest
    ) {
        return requestService.changeRequestStatus(userId, eventId, updateRequest);
    }
}
