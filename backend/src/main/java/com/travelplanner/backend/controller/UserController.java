package com.travelplanner.backend.controller;

import com.travelplanner.backend.model.User;
import com.travelplanner.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @PostMapping("/sync")
    public ResponseEntity<User> syncUser(@RequestBody User userData) {
        User savedUser = userRepository.save(userData);
        return ResponseEntity.ok(savedUser);
    }
}