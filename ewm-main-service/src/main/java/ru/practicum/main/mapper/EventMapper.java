package ru.practicum.main.mapper;

import ru.practicum.main.model.Event;
import ru.practicum.main.model.EventState;
import ru.practicum.main.dto.event.*;
import ru.practicum.main.dto.category.CategoryDto;
import ru.practicum.main.dto.user.UserShortDto;

import java.time.LocalDateTime;

public class EventMapper {

    public static Event toEntity(NewEventDto dto, Long initiatorId) {
        return Event.builder()
                .annotation(dto.getAnnotation())
                .description(dto.getDescription())
                .title(dto.getTitle())
                .categoryId(dto.getCategory())
                .eventDate(dto.getEventDate())
                .locationLat(dto.getLocation().getLat())
                .locationLon(dto.getLocation().getLon())
                .paid(dto.getPaid() != null ? dto.getPaid() : false)
                .participantLimit(dto.getParticipantLimit() != null ? dto.getParticipantLimit() : 0)
                .requestModeration(dto.getRequestModeration() != null ? dto.getRequestModeration() : true)
                .initiatorId(initiatorId)
                .state(EventState.PENDING)
                .createdOn(LocalDateTime.now())
                .build();
    }

    public static EventShortDto toShort(Event e, CategoryDto catDto, UserShortDto userDto, long views, long confirmedRequests) {
        return new EventShortDto(
                e.getId(), e.getAnnotation(), catDto, confirmedRequests,
                e.getEventDate(), userDto, e.getPaid(), e.getTitle(), views
        );
    }

    public static EventShortDto toShort(Event e, long views) {
        return new EventShortDto(
                e.getId(), e.getAnnotation(), null, 0L,
                e.getEventDate(), null, e.getPaid(), e.getTitle(), views
        );
    }

    public static EventFullDto toFull(Event e, CategoryDto catDto, UserShortDto userDto, long views, long confirmedRequests) {
        LocationDto loc = new LocationDto(e.getLocationLat(), e.getLocationLon());

        return new EventFullDto(
                e.getId(), e.getAnnotation(), e.getDescription(), catDto,
                confirmedRequests, e.getEventDate(), userDto,
                loc, e.getPaid(), e.getParticipantLimit(), e.getRequestModeration(),
                e.getState(), e.getCreatedOn(), e.getPublishedOn(),
                e.getTitle(), views
        );
    }

    public static void applyUserUpdate(Event e, UpdateEventUserRequest dto) {
        if (dto.getAnnotation() != null) e.setAnnotation(dto.getAnnotation());
        if (dto.getDescription() != null) e.setDescription(dto.getDescription());
        if (dto.getTitle() != null) e.setTitle(dto.getTitle());
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
        if (dto.getAnnotation() != null) e.setAnnotation(dto.getAnnotation());
        if (dto.getDescription() != null) e.setDescription(dto.getDescription());
        if (dto.getTitle() != null) e.setTitle(dto.getTitle());
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
}