package com.url.shortner.services;

import com.url.shortner.dtos.LoginRequest;
import com.url.shortner.models.User;
import com.url.shortner.repository.UserRepository;
import com.url.shortner.security.jwt.JwtAuthenticationResponse;
import com.url.shortner.security.jwt.JwtUtils;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {

    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    AuthenticationManager authenticationManager;
    JwtUtils jwtUtils;

    /**
     * Registration Business Logic
     * Encrypts the raw password before it reaches the Database Layer.
     */
    public void registerUser(User user){
        // Step 3.1: Password Encoding (using BCrypt)
        // Ensures raw passwords are never stored in the database.
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Step 4: Persistence
        userRepository.save(user);
    }

    /**
     * Login/Authentication Business Logic
     * Verifies credentials and generates a stateful session via JWT.
     */
    public JwtAuthenticationResponse authenticateUser(LoginRequest loginRequest) {
        // Step 3: Internal Execution via AuthenticationManager
        // 1. Calls UserDetailsServiceImpl to fetch user from DB.
        // 2. DaoAuthenticationProvider compares the raw password vs hashed password.
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );

        // Step 4.1: Update Security Context
        // This marks the user as "authenticated" for the current request thread.
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Step 4.2: Extract User Details and Generate Token
        // UserDetailsImpl acts as the 'Passport' for the user.
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Signed with HS384 algorithm and secret key from application.properties
        String jwt = jwtUtils.generateToken(userDetails);

        // Step 5: Wrap token in DTO for client response
        return new JwtAuthenticationResponse(jwt);
    }

    public User getByUsername(String name) {
        return userRepository.findByUsername(name).orElseThrow(
                ()->new UsernameNotFoundException("User with this username:- "+name+" not found in database")
        );
    }
}