package com.travelplanner.backend.service;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.travelplanner.backend.dto.CreateTripRequest;
import com.travelplanner.backend.dto.TripResponse;
import com.travelplanner.backend.model.Trip;
import com.travelplanner.backend.model.User;
import com.travelplanner.backend.repository.TripMemberRepository;
import com.travelplanner.backend.repository.TripRepository;
import com.travelplanner.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class TripService {

    private static final char[] CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();
    private static final int INVITE_CODE_LENGTH = 10;
    private static final int CODE_GENERATION_ATTEMPTS = 5;

    private final TripRepository tripRepository;
    private final TripMemberRepository tripMemberRepository;
    private final UserRepository userRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional(readOnly = true)
    public List<TripResponse> getTrips(UUID userId) {
        requireUser(userId);
        return tripRepository.findAllForMember(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public TripResponse createTrip(UUID userId, CreateTripRequest request) {
        User owner = requireUser(userId);
        if (request == null || request.name() == null || request.name().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Numele excursiei este obligatoriu.");
        }
        if (hasInvalidDateRange(request.startDate(), request.endDate())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Data de început trebuie să fie înaintea datei de sfârșit.");
        }

        Trip trip = new Trip(
                request.name().trim(),
                normalizeOptionalText(request.destination()),
                request.startDate(),
                request.endDate(),
                generateInviteCode(),
                owner);
        trip.addMember(owner);
        return toResponse(tripRepository.save(trip));
    }

    @Transactional
    public TripResponse joinTrip(UUID userId, String inviteCode) {
        requireUser(userId);
        if (inviteCode == null || inviteCode.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Codul de invitație este obligatoriu.");
        }

        String normalizedCode = inviteCode.trim().toUpperCase();
        Trip trip = tripRepository.findByInviteCodeWithMembers(normalizedCode)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Nu am găsit nicio excursie cu acest cod."));

        tripMemberRepository.insertIfAbsent(
                UUID.randomUUID(),
                trip.getId(),
                userId,
                LocalDateTime.now());
        int memberCount = Math.toIntExact(tripMemberRepository.countByTrip_Id(trip.getId()));
        return toResponse(trip, memberCount);
    }

    private User requireUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Utilizatorul nu este sincronizat cu backend-ul."));
    }

    private String generateInviteCode() {
        for (int attempt = 0; attempt < CODE_GENERATION_ATTEMPTS; attempt++) {
            StringBuilder code = new StringBuilder(INVITE_CODE_LENGTH);
            for (int index = 0; index < INVITE_CODE_LENGTH; index++) {
                code.append(CODE_ALPHABET[secureRandom.nextInt(CODE_ALPHABET.length)]);
            }
            String candidate = code.toString();
            if (!tripRepository.existsByInviteCode(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Nu s-a putut genera un cod unic de invitație.");
    }

    private boolean hasInvalidDateRange(LocalDate startDate, LocalDate endDate) {
        return startDate != null && endDate != null && startDate.isAfter(endDate);
    }

    private String normalizeOptionalText(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private TripResponse toResponse(Trip trip) {
        return toResponse(trip, trip.getMembers().size());
    }

    private TripResponse toResponse(Trip trip, int memberCount) {
        User owner = trip.getOwner();
        return new TripResponse(
                trip.getId(),
                trip.getName(),
                trip.getDestination(),
                trip.getStartDate(),
                trip.getEndDate(),
                trip.getInviteCode(),
                owner.getId(),
                owner.getFullName(),
                memberCount,
                trip.getCreatedAt());
    }
}
