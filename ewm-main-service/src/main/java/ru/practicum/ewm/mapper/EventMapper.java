package ru.practicum.ewm.mapper;

import ru.practicum.ewm.dto.*;
import ru.practicum.ewm.dto.*;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.EventState;

import java.time.LocalDateTime;

public class EventMapper {

    public static Event toEntity(NewEventDto dto, Long initiatorId) {
        return Event.builder()
                .annotation(dto.getAnnotation())
                .description(dto.getDescription())
                .categoryId(dto.getCategory())
                .eventDate(dto.getEventDate())
                .locationLat(dto.getLocation().getLat())
                .locationLon(dto.getLocation().getLon())
                .paid(dto.getPaid())
                .participantLimit(dto.getParticipantLimit())
                .requestModeration(dto.getRequestModeration())
                .initiatorId(initiatorId)
                .state(EventState.PENDING)
                .createdOn(LocalDateTime.now())
                .build();
    }

    public static EventShortDto toShort(Event e, long views) {
        return new EventShortDto(e.getId(), e.getAnnotation(), e.getCategoryId(), e.getEventDate(), e.getPaid(), views);
    }

    public static EventFullDto toFull(Event e, long views) {
        LocationDto loc = new LocationDto();
        loc.setLat(e.getLocationLat());
        loc.setLon(e.getLocationLon());
        return new EventFullDto(
                e.getId(), e.getAnnotation(), e.getDescription(), e.getCategoryId(),
                e.getEventDate(), loc, e.getPaid(), e.getParticipantLimit(), e.getRequestModeration(),
                e.getInitiatorId(), e.getState(), e.getCreatedOn(), e.getPublishedOn(), views
        );
    }

    public static void applyUserUpdate(Event e, UpdateEventUserRequest dto) {
        if (dto.getAnnotation() != null) e.setAnnotation(dto.getAnnotation());
        if (dto.getDescription() != null) e.setDescription(dto.getDescription());
        if (dto.getCategory() != null) e.setCategoryId(dto.getCategory());
        if (dto.getEventDate() != null) e.setEventDate(dto.getEventDate());
        if (dto.getLocation() != null) {
            e.setLocationLat(dto.getLocation().getLat());
            e.setLocationLon(dto.getLocation().getLon());
        }
        if (dto.getPaid() != null) e.setPaid(dto.getPaid());
        if (dto.getParticipantLimit() != null) e.setParticipantLimit(dto.getParticipantLimit());
        if (dto.getRequestModeration() != null) e.setRequestModeration(dto.getRequestModeration());
    }

    public static void applyAdminUpdate(Event e, UpdateEventAdminRequest dto) {
        applyUserUpdate(e, toUserUpdate(dto));
    }

    private static UpdateEventUserRequest toUserUpdate(UpdateEventAdminRequest dto) {
        UpdateEventUserRequest u = new UpdateEventUserRequest();
        u.setAnnotation(dto.getAnnotation());
        u.setDescription(dto.getDescription());
        u.setCategory(dto.getCategory());
        u.setEventDate(dto.getEventDate());
        u.setLocation(dto.getLocation());
        u.setPaid(dto.getPaid());
        u.setParticipantLimit(dto.getParticipantLimit());
        u.setRequestModeration(dto.getRequestModeration());
        return u;
    }
}