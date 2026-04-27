package com.url.shortner.services;

import com.url.shortner.dtos.LoginRequest;
import com.url.shortner.models.User;
import com.url.shortner.repository.UserRepository;
import com.url.shortner.security.jwt.JwtAuthenticationResponse;
import com.url.shortner.security.jwt.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtUtils jwtUtils;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setUsername("testuser");
        testUser.setPassword("rawPassword");
        testUser.setRole("ROLE_USER");
    }

    // --- registerUser ---
    @Nested @DisplayName("registerUser()")
    class RegisterUser {

        @Test @DisplayName("should encode password before saving")
        void encodesPassword() {
            when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");
            userService.registerUser(testUser);

            assertEquals("encodedPassword", testUser.getPassword());
            verify(passwordEncoder).encode("rawPassword");
            verify(userRepository).save(testUser);
        }

        @Test @DisplayName("should call repository.save exactly once")
        void savesOnce() {
            when(passwordEncoder.encode(any())).thenReturn("enc");
            userService.registerUser(testUser);
            verify(userRepository, times(1)).save(testUser);
        }

        @Test @DisplayName("should pass the same user object to save (not a copy)")
        void passesOriginalObject() {
            when(passwordEncoder.encode(any())).thenReturn("enc");
            ArgumentCaptor<User> cap = ArgumentCaptor.forClass(User.class);
            userService.registerUser(testUser);
            verify(userRepository).save(cap.capture());
            assertSame(testUser, cap.getValue());
        }
    }

    // --- authenticateUser ---
    @Nested @DisplayName("authenticateUser()")
    class AuthenticateUser {

        @Test @DisplayName("should return JWT response on valid credentials")
        void returnsJwt() {
            LoginRequest req = new LoginRequest();
            req.setUsername("testuser");
            req.setPassword("rawPassword");

            UserDetailsImpl userDetails = UserDetailsImpl.build(testUser);
            Authentication mockAuth = mock(Authentication.class);
            when(mockAuth.getPrincipal()).thenReturn(userDetails);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(mockAuth);
            when(jwtUtils.generateToken(userDetails)).thenReturn("mock.jwt.token");

            JwtAuthenticationResponse response = userService.authenticateUser(req);

            assertNotNull(response);
            assertEquals("mock.jwt.token", response.getToken());
        }

        @Test @DisplayName("should call authenticationManager.authenticate with correct credentials")
        void callsAuthManager() {
            LoginRequest req = new LoginRequest();
            req.setUsername("testuser");
            req.setPassword("rawPassword");

            UserDetailsImpl userDetails = UserDetailsImpl.build(testUser);
            Authentication mockAuth = mock(Authentication.class);
            when(mockAuth.getPrincipal()).thenReturn(userDetails);
            when(authenticationManager.authenticate(any())).thenReturn(mockAuth);
            when(jwtUtils.generateToken(any())).thenReturn("token");

            userService.authenticateUser(req);

            ArgumentCaptor<UsernamePasswordAuthenticationToken> cap =
                    ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
            verify(authenticationManager).authenticate(cap.capture());
            assertEquals("testuser", cap.getValue().getPrincipal());
            assertEquals("rawPassword", cap.getValue().getCredentials());
        }

        @Test @DisplayName("should propagate BadCredentialsException on invalid credentials")
        void propagatesBadCredentials() {
            LoginRequest req = new LoginRequest();
            req.setUsername("testuser");
            req.setPassword("wrong");
            when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

            assertThrows(BadCredentialsException.class, () -> userService.authenticateUser(req));
        }

        @Test @DisplayName("should call jwtUtils.generateToken with correct UserDetailsImpl")
        void callsGenerateToken() {
            LoginRequest req = new LoginRequest();
            req.setUsername("testuser");
            req.setPassword("rawPassword");

            UserDetailsImpl userDetails = UserDetailsImpl.build(testUser);
            Authentication mockAuth = mock(Authentication.class);
            when(mockAuth.getPrincipal()).thenReturn(userDetails);
            when(authenticationManager.authenticate(any())).thenReturn(mockAuth);
            when(jwtUtils.generateToken(any(UserDetailsImpl.class))).thenReturn("t");

            userService.authenticateUser(req);

            ArgumentCaptor<UserDetailsImpl> cap = ArgumentCaptor.forClass(UserDetailsImpl.class);
            verify(jwtUtils).generateToken(cap.capture());
            assertEquals("testuser", cap.getValue().getUsername());
        }
    }

    // --- getByUsername ---
    @Nested @DisplayName("getByUsername()")
    class GetByUsername {

        @Test @DisplayName("should return user when username exists")
        void returnsUser() {
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            User result = userService.getByUsername("testuser");
            assertNotNull(result);
            assertEquals("testuser", result.getUsername());
            assertEquals("test@example.com", result.getEmail());
        }

        @Test @DisplayName("should throw UsernameNotFoundException when user not found")
        void throwsWhenNotFound() {
            when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());
            UsernameNotFoundException ex = assertThrows(
                    UsernameNotFoundException.class,
                    () -> userService.getByUsername("unknown")
            );
            assertTrue(ex.getMessage().contains("unknown"));
        }

        @Test @DisplayName("should call repository exactly once")
        void callsRepoOnce() {
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            userService.getByUsername("testuser");
            verify(userRepository, times(1)).findByUsername("testuser");
        }
    }
}
