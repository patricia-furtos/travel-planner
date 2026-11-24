package com.travelplanner.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.time.LocalDateTime;
import java.util.UUID;

import com.travelplanner.backend.dto.CreateTripRequest;
import com.travelplanner.backend.dto.TripResponse;
import com.travelplanner.backend.model.Trip;
import com.travelplanner.backend.model.User;
import com.travelplanner.backend.repository.TripMemberRepository;
import com.travelplanner.backend.repository.TripRepository;
import com.travelplanner.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class TripServiceTests {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private TripMemberRepository tripMemberRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TripService tripService;

    @Test
    void createTripGeneratesInviteCodeAndAddsOwnerAsMember() {
        UUID userId = UUID.randomUUID();
        User owner = new User(userId, "owner@example.com", "Trip Owner", null);
        when(userRepository.findById(userId)).thenReturn(Optional.of(owner));
        when(tripRepository.existsByInviteCode(anyString())).thenReturn(false);
        when(tripRepository.save(any(Trip.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TripResponse created = tripService.createTrip(
                userId,
                new CreateTripRequest(" Summer trip ", " Rome ", null, null));

        assertThat(created.name()).isEqualTo("Summer trip");
        assertThat(created.destination()).isEqualTo("Rome");
        assertThat(created.inviteCode()).matches("[A-HJ-NP-Z2-9]{10}");
        assertThat(created.ownerId()).isEqualTo(userId);
        assertThat(created.memberCount()).isEqualTo(1);
    }

    @Test
    void joinTripIsIdempotentForAnExistingMember() {
        UUID userId = UUID.randomUUID();
        User owner = new User(userId, "owner@example.com", "Trip Owner", null);
        Trip trip = new Trip("Weekend", null, null, null, "ABCD2345EF", owner);
        trip.addMember(owner);
        when(userRepository.findById(userId)).thenReturn(Optional.of(owner));
        when(tripRepository.findByInviteCodeWithMembers("ABCD2345EF")).thenReturn(Optional.of(trip));
        when(tripMemberRepository.insertIfAbsent(
                any(UUID.class), eq(trip.getId()), eq(userId), any(LocalDateTime.class))).thenReturn(0);
        when(tripMemberRepository.countByTrip_Id(trip.getId())).thenReturn(1L);

        TripResponse joined = tripService.joinTrip(userId, " abcd2345ef ");

        assertThat(joined.memberCount()).isEqualTo(1);
        verify(tripMemberRepository).insertIfAbsent(
                any(UUID.class), eq(trip.getId()), eq(userId), any(LocalDateTime.class));
    }

    @Test
    void joinTripAddsANewMember() {
        UUID ownerId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        User owner = new User(ownerId, "owner@example.com", "Trip Owner", null);
        User member = new User(memberId, "member@example.com", "New Member", null);
        Trip trip = new Trip("Weekend", null, null, null, "ABCD2345EF", owner);
        trip.addMember(owner);
        when(userRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(tripRepository.findByInviteCodeWithMembers("ABCD2345EF")).thenReturn(Optional.of(trip));
        when(tripMemberRepository.insertIfAbsent(
                any(UUID.class), eq(trip.getId()), eq(memberId), any(LocalDateTime.class))).thenReturn(1);
        when(tripMemberRepository.countByTrip_Id(trip.getId())).thenReturn(2L);

        TripResponse joined = tripService.joinTrip(memberId, "ABCD2345EF");

        assertThat(joined.memberCount()).isEqualTo(2);
        verify(tripMemberRepository).insertIfAbsent(
                any(UUID.class), eq(trip.getId()), eq(memberId), any(LocalDateTime.class));
    }

    @Test
    void createTripRejectsEndDateBeforeStartDate() {
        UUID userId = UUID.randomUUID();
        User owner = new User(userId, "owner@example.com", "Trip Owner", null);
        when(userRepository.findById(userId)).thenReturn(Optional.of(owner));

        assertThatThrownBy(() -> tripService.createTrip(
                userId,
                new CreateTripRequest("Weekend", null,
                        java.time.LocalDate.parse("2026-06-10"),
                        java.time.LocalDate.parse("2026-06-09"))))
                .isInstanceOf(ResponseStatusException.class);
        verify(tripRepository, never()).save(any(Trip.class));
    }
}
