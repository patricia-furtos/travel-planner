package com.travelplanner.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.travelplanner.backend.model.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TripRepository extends JpaRepository<Trip, UUID> {

    boolean existsByInviteCode(String inviteCode);

    @Query("""
            select distinct trip
            from Trip trip
            join fetch trip.members
            left join fetch trip.owner
            where trip.id in (
                select membership.trip.id
                from TripMember membership
                where membership.user.id = :userId
            )
            order by trip.createdAt desc
            """)
    List<Trip> findAllForMember(@Param("userId") UUID userId);

    @Query("""
            select distinct trip
            from Trip trip
            join fetch trip.members
            left join fetch trip.owner
            where trip.inviteCode = :inviteCode
            """)
    Optional<Trip> findByInviteCodeWithMembers(@Param("inviteCode") String inviteCode);
}
