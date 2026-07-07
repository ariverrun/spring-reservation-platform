package com.example.reservation.repository;

import com.example.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.event.id = :eventId")
    long countByEventId(UUID eventId);

    @Query("SELECT r FROM Reservation r JOIN FETCH r.event WHERE r.userId = :userId ORDER BY r.id DESC")
    List<Reservation> findAllByUserIdWithEvent(@Param("userId") UUID userId);
}