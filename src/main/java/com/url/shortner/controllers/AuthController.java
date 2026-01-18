package com.url.shortner.controllers;

import com.url.shortner.dtos.LoginRequest;
import com.url.shortner.dtos.RegisterRequest;
import com.url.shortner.models.User;
import com.url.shortner.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {

    private UserService userService;

    /**
     * Endpoint: POST /api/auth/public/register
     * Process: Receives user details, creates a User entity with a default 'ROLE_USER',
     * and delegates the sensitive processing (encryption/persistence) to the UserService.
     */
    @PostMapping("/public/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest){
        // Step 2 in Flow: Mapping JSON to User Entity
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(registerRequest.getPassword());
        user.setRole("ROLE_USER"); // Default role assignment

        // Step 3: Delegation to Service Layer
        userService.registerUser(user);

        return ResponseEntity.ok("user registered successfully");
    }

    /**
     * Endpoint: POST /api/auth/public/login
     * Process: Accepts credentials and returns a JWT if authentication is successful.
     */
    @PostMapping("/public/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest){
        // Returns a JwtAuthenticationResponse containing the generated token
        return ResponseEntity.ok(userService.authenticateUser(loginRequest));
    }
}