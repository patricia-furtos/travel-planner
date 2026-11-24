package com.travelplanner.backend.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record TripResponse(
        UUID id,
        String name,
        String destination,
        LocalDate startDate,
        LocalDate endDate,
        String inviteCode,
        UUID ownerId,
        String ownerName,
        int memberCount,
        LocalDateTime createdAt) {
}
