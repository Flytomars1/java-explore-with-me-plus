package ru.practicum.main.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.main.model.ParticipationRequest;
import ru.practicum.main.model.RequestStatus;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<ParticipationRequest, Long> {

    boolean existsByRequester_IdAndEvent_Id(Long requesterId, Long eventId);

    Optional<ParticipationRequest> findByIdAndRequester_Id(Long requestId, Long requesterId);

    List<ParticipationRequest> findAllByRequester_IdOrderByCreatedDesc(Long requesterId);

    List<ParticipationRequest> findAllByEvent_IdOrderByCreatedAsc(Long eventId);

    List<ParticipationRequest> findAllByIdInAndEvent_Id(List<Long> ids, Long eventId);

    long countByEvent_IdAndStatus(Long eventId, RequestStatus status);
}
