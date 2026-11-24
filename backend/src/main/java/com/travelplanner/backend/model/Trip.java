package com.travelplanner.backend.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "trips")
@Getter
@NoArgsConstructor
public class Trip {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String destination;

    private LocalDate startDate;

    private LocalDate endDate;

    @Column(nullable = false, unique = true, length = 12)
    private String inviteCode;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TripMember> members = new ArrayList<>();

    public Trip(
            String name,
            String destination,
            LocalDate startDate,
            LocalDate endDate,
            String inviteCode,
            User owner) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.destination = destination;
        this.startDate = startDate;
        this.endDate = endDate;
        this.inviteCode = inviteCode;
        this.owner = owner;
        this.createdAt = LocalDateTime.now();
    }

    public void addMember(User user) {
        members.add(new TripMember(this, user));
    }
}
