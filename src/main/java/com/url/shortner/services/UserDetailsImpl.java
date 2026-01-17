package com.url.shortner.services;

import com.url.shortner.models.User;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

// this class will act as a bridge between our custom-made user and spring security's predefined user
// it tells spring that you have to authenticate this user
// this class is needed to represent the authenticated user in spring security
/**
 * THINK OF THIS CLASS AS A "PASSPORT".
 * * In your database, you have a 'User' (like a person's birth certificate).
 * But Spring Security is like an Airport Guard—it doesn't recognize your birth certificate;
 * it only recognizes a "Passport" (the UserDetails interface).
 * * This class takes your database information and puts it into the "Passport"
 * format so Spring Security can read it.
 */
@Data
@NoArgsConstructor
public class UserDetailsImpl implements UserDetails {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String email;
    private String username;
    private String password;
    /**
     * Authorities are the permissions or roles granted to the user (e.g., ROLE_USER, ROLE_ADMIN).
     * These are checked by Spring Security to authorize access to specific endpoints.
     */
    private Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(Long id, String email, String username, String password,
            Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.password = password;
        this.authorities = authorities;
    }

    /**
     * THE CONVERTER (Static Factory Method)
     * ------------------------------------
     * This is like a machine: You put in a 'User' from the database,
     * and it spits out a 'UserDetailsImpl' for Spring Security.
     */
    // Converts our User entity to a Spring Security UserDetails object
    public static UserDetailsImpl build(User user) {
        // Create an authority (role) based on the user's role
        GrantedAuthority authority = new SimpleGrantedAuthority(user.getRole());
        return new UserDetailsImpl(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(authority) // User has only one role in this case
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities; // Returns the roles/authorities granted to the user
    }

    @Override
    public String getPassword() {
        return password; // Returns the password used to authenticate the user
    }

    @Override
    public String getUsername() {
        return username; // Returns the username used to authenticate the user
    }
}
