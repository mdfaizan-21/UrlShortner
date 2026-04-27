package com.url.shortner.services;

import com.url.shortner.models.User;
import com.url.shortner.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserDetailsServiceImpl Tests")
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setUsername("testuser");
        testUser.setPassword("encodedPassword");
        testUser.setRole("ROLE_USER");
    }

    @Test
    @DisplayName("loadUserByUsername should return UserDetails when user exists")
    void loadUserByUsername_userExists_returnsUserDetails() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        UserDetails result = userDetailsService.loadUserByUsername("testuser");

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("encodedPassword", result.getPassword());
        assertFalse(result.getAuthorities().isEmpty());
        assertEquals("ROLE_USER", result.getAuthorities().iterator().next().getAuthority());
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    @DisplayName("loadUserByUsername should throw UsernameNotFoundException when user not found")
    void loadUserByUsername_userNotFound_throwsException() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("unknown")
        );
        assertTrue(exception.getMessage().contains("unknown"));
        verify(userRepository).findByUsername("unknown");
    }

    @Test
    @DisplayName("loadUserByUsername should return UserDetailsImpl instance")
    void loadUserByUsername_returnsUserDetailsImplInstance() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        UserDetails result = userDetailsService.loadUserByUsername("testuser");

        assertInstanceOf(UserDetailsImpl.class, result);
        UserDetailsImpl impl = (UserDetailsImpl) result;
        assertEquals(1L, impl.getId());
        assertEquals("test@example.com", impl.getEmail());
    }

    @Test
    @DisplayName("loadUserByUsername should handle admin role correctly")
    void loadUserByUsername_adminRole() {
        testUser.setRole("ROLE_ADMIN");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        UserDetails result = userDetailsService.loadUserByUsername("testuser");

        assertEquals("ROLE_ADMIN", result.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    @DisplayName("loadUserByUsername should call repository exactly once")
    void loadUserByUsername_callsRepositoryOnce() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        userDetailsService.loadUserByUsername("testuser");

        verify(userRepository, times(1)).findByUsername("testuser");
        verifyNoMoreInteractions(userRepository);
    }
}
