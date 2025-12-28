package ru.practicum.main.mapper;

import ru.practicum.main.dto.request.ParticipationRequestDto;
import ru.practicum.main.model.ParticipationRequest;

public final class RequestMapper {

    private RequestMapper() {
    }

    public static ParticipationRequestDto toDto(ParticipationRequest request) {
        if (request == null) {
            return null;
        }
        return ParticipationRequestDto.builder()
                .id(request.getId())
                .created(request.getCreated())
                .event(request.getEvent() != null ? request.getEvent().getId() : null)
                .requester(request.getRequester() != null ? request.getRequester().getId() : null)
                .status(request.getStatus() != null ? request.getStatus().name() : null)
                .build();
    }
}
