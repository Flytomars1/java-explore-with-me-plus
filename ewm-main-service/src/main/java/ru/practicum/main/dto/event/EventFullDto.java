package ru.practicum.main.dto.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.main.dto.category.CategoryDto;
import ru.practicum.main.dto.rating.RatingDto;
import ru.practicum.main.dto.user.UserShortDto;
import ru.practicum.main.model.EventState;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventFullDto {
    private Long id;
    private String annotation;
    private String description;
    private CategoryDto category;
    private Long confirmedRequests;
    private String eventDate;
    private UserShortDto initiator;
    private LocationDto location;
    private Boolean paid;
    private Integer participantLimit;
    private Boolean requestModeration;
    private EventState state;
    private String createdOn;
    private String publishedOn;
    private String title;
    private Long views;
    private RatingDto rating;

    public EventFullDto(Long id, String annotation, String description, CategoryDto category,
                        Long confirmedRequests, String eventDate, UserShortDto initiator,
                        LocationDto location, Boolean paid, Integer participantLimit,
                        Boolean requestModeration, EventState state, String createdOn,
                        String publishedOn, String title, Long views) {
        this(id, annotation, description, category, confirmedRequests, eventDate, initiator,
                location, paid, participantLimit, requestModeration, state, createdOn,
                publishedOn, title, views, null);
    }
}