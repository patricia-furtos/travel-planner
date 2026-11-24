package com.travelplanner.backend.dto;

import java.time.LocalDate;

public record CreateTripRequest(
        String name,
        String destination,
        LocalDate startDate,
        LocalDate endDate) {
}
