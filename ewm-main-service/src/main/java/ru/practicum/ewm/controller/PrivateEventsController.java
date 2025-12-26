package ru.practicum.ewm.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import ru.practicum.ewm.dto.EventFullDto;
import ru.practicum.ewm.dto.EventShortDto;
import ru.practicum.ewm.dto.NewEventDto;
import ru.practicum.ewm.dto.UpdateEventUserRequest;
import ru.practicum.ewm.dto.*;
import ru.practicum.ewm.service.EventService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/events")
public class PrivateEventsController {

    private final EventService service;

    @PostMapping
    public EventFullDto create(@PathVariable Long userId, @Valid @RequestBody NewEventDto dto) {
        return service.create(userId, dto);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto update(@PathVariable Long userId, @PathVariable Long eventId, @Valid @RequestBody UpdateEventUserRequest dto) {
        return service.updateByUser(userId, eventId, dto);
    }

    @GetMapping
    public Page<EventShortDto> list(@PathVariable Long userId,
                                    @RequestParam(defaultValue = "0") @Min(0) Integer from,
                                    @RequestParam(defaultValue = "10") @Min(1) Integer size) {
        return service.getUserEvents(userId, from, size);
    }

    @GetMapping("/{eventId}")
    public EventFullDto get(@PathVariable Long userId, @PathVariable Long eventId) {
        return service.getUserEvent(userId, eventId);
    }
}