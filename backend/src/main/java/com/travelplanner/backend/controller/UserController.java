package com.travelplanner.backend.controller;

import java.util.UUID;

import com.travelplanner.backend.dto.UserSyncRequest;
import com.travelplanner.backend.model.User;
import com.travelplanner.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @PostMapping("/sync")
    public ResponseEntity<User> syncUser(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody UserSyncRequest profile) {
        UUID userId = userIdFrom(jwt);
        String email = jwt.getClaimAsString("email");
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Tokenul Supabase nu conține un email.");
        }

        User user = userRepository.findById(userId)
                .orElseGet(() -> new User(userId, email, null, null));
        user.setEmail(email);
        user.setFullName(profile == null ? null : profile.fullName());
        user.setAvatarUrl(profile == null ? null : profile.avatarUrl());

        User savedUser = userRepository.save(user);
        return ResponseEntity.ok(savedUser);
    }

    private UUID userIdFrom(Jwt jwt) {
        if (jwt == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Autentificarea este obligatorie.");
        }
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