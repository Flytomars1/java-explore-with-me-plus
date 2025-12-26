package ru.practicum.ewm.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NewEventDto {
    @NotBlank
    @Size(min = 1, max = 2000)
    private String annotation;

    @NotBlank
    @Size(min = 1, max = 20000)
    private String description;

    @NotNull
    private Long category;

    @NotNull
    @Future
    private LocalDateTime eventDate;

    @NotNull
    private LocationDto location;

    @NotNull
    private Boolean paid;

    @NotNull
    private Integer participantLimit;

    @NotNull
    private Boolean requestModeration;
}