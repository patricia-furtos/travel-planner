package com.travelplanner.backend.controller;

import java.util.List;
import java.util.UUID;

import com.travelplanner.backend.dto.CreateTripRequest;
import com.travelplanner.backend.dto.JoinTripRequest;
import com.travelplanner.backend.dto.TripResponse;
import com.travelplanner.backend.service.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    @GetMapping
    public List<TripResponse> getTrips(@AuthenticationPrincipal Jwt jwt) {
        return tripService.getTrips(userIdFrom(jwt));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TripResponse createTrip(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody CreateTripRequest request) {
        return tripService.createTrip(userIdFrom(jwt), request);
    }

    @PostMapping("/join")
    public TripResponse joinTrip(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody JoinTripRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Codul de invitație este obligatoriu.");
        }
        return tripService.joinTrip(userIdFrom(jwt), request.inviteCode());
    }

    private UUID userIdFrom(Jwt jwt) {
        try {
            return UUID.fromString(jwt.getSubject());
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Token Supabase invalid.",
                    exception);
        }
    }
}
