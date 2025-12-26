package ru.practicum.ewm.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateEventUserRequest {
    @Size(min = 1, max = 2000)
    private String annotation;
    @Size(min = 1, max = 20000)
    private String description;
    private Long category;
    private LocalDateTime eventDate;
    private LocationDto location;
    private Boolean paid;
    private Integer participantLimit;
    private Boolean requestModeration;
}