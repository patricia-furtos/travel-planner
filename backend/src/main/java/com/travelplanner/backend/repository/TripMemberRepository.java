package com.travelplanner.backend.repository;

import java.time.LocalDateTime;
import java.util.UUID;

import com.travelplanner.backend.model.TripMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TripMemberRepository extends JpaRepository<TripMember, UUID> {

    @Modifying
    @Query(value = """
            insert into trip_members (id, trip_id, user_id, joined_at)
            values (:id, :tripId, :userId, :joinedAt)
            on conflict (trip_id, user_id) do nothing
            """, nativeQuery = true)
    int insertIfAbsent(
            @Param("id") UUID id,
            @Param("tripId") UUID tripId,
            @Param("userId") UUID userId,
            @Param("joinedAt") LocalDateTime joinedAt);

    long countByTrip_Id(UUID tripId);
}
