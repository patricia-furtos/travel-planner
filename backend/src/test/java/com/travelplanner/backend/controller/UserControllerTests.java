package com.travelplanner.backend.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import com.travelplanner.backend.dto.UserSyncRequest;
import com.travelplanner.backend.model.User;
import com.travelplanner.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

@ExtendWith(MockitoExtension.class)
class UserControllerTests {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserController userController;

    @Test
    void syncUsesIdentityAndEmailFromVerifiedToken() {
        UUID userId = UUID.randomUUID();
        Instant now = Instant.now();
        Jwt jwt = Jwt.withTokenValue("signed-token")
                .header("alg", "RS256")
                .subject(userId.toString())
                .claim("email", "verified@example.com")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(60))
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User synced = userController.syncUser(jwt, new UserSyncRequest("Verified Name", null)).getBody();

        assertThat(synced).isNotNull();
        assertThat(synced.getId()).isEqualTo(userId);
        assertThat(synced.getEmail()).isEqualTo("verified@example.com");
        assertThat(synced.getFullName()).isEqualTo("Verified Name");
    }
}
