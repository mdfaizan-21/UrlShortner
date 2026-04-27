package com.url.shortner.services;

import com.url.shortner.models.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UserDetailsImpl Tests")
class UserDetailsImplTest {

    private User createUser(Long id, String email, String username, String password, String role) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setUsername(username);
        user.setPassword(password);
        user.setRole(role);
        return user;
    }

    @Test
    @DisplayName("build() should correctly map all User fields to UserDetailsImpl")
    void buildMapsAllFields() {
        User user = createUser(1L, "test@example.com", "testuser", "encodedPass", "ROLE_USER");
        UserDetailsImpl details = UserDetailsImpl.build(user);

        assertEquals(1L, details.getId());
        assertEquals("test@example.com", details.getEmail());
        assertEquals("testuser", details.getUsername());
        assertEquals("encodedPass", details.getPassword());
    }

    @Test
    @DisplayName("build() should assign the user's role as a GrantedAuthority")
    void buildAssignsAuthority() {
        User user = createUser(1L, "a@b.com", "admin", "pass", "ROLE_ADMIN");
        UserDetailsImpl details = UserDetailsImpl.build(user);

        Collection<? extends GrantedAuthority> authorities = details.getAuthorities();
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertEquals("ROLE_ADMIN", authorities.iterator().next().getAuthority());
    }

    @Test
    @DisplayName("build() should handle default ROLE_USER")
    void buildHandlesDefaultRole() {
        User user = createUser(2L, "u@v.com", "user2", "pw", "ROLE_USER");
        UserDetailsImpl details = UserDetailsImpl.build(user);

        assertEquals("ROLE_USER", details.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    @DisplayName("getUsername() should return the username")
    void getUsernameReturnsUsername() {
        UserDetailsImpl details = new UserDetailsImpl();
        details.setUsername("myuser");
        assertEquals("myuser", details.getUsername());
    }

    @Test
    @DisplayName("getPassword() should return the password")
    void getPasswordReturnsPassword() {
        UserDetailsImpl details = new UserDetailsImpl();
        details.setPassword("secret");
        assertEquals("secret", details.getPassword());
    }

    @Test
    @DisplayName("getAuthorities() should return the collection set via constructor")
    void getAuthoritiesFromConstructor() {
        User user = createUser(1L, "e@f.com", "u1", "p1", "ROLE_MODERATOR");
        UserDetailsImpl details = UserDetailsImpl.build(user);

        assertNotNull(details.getAuthorities());
        assertTrue(details.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MODERATOR")));
    }

    @Test
    @DisplayName("no-arg constructor should create instance with null fields")
    void noArgConstructor() {
        UserDetailsImpl details = new UserDetailsImpl();
        assertNull(details.getId());
        assertNull(details.getEmail());
        assertNull(details.getUsername());
        assertNull(details.getPassword());
        assertNull(details.getAuthorities());
    }

}
