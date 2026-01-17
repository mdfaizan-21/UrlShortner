package com.url.shortner.security;

import com.url.shortner.security.jwt.JwtAuthenticationFilter;
import com.url.shortner.services.UserDetailsServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * This class is the heart of your security layer.
 * It tells Spring Security how to handle users, passwords, and which URLs are private or public.
 */
@Configuration // Tells Spring this class contains configuration beans
@EnableWebSecurity // Enables Spring Security's web support
@EnableMethodSecurity // Allows you to secure specific methods using annotations like @PreAuthorize
@AllArgsConstructor // Automatically creates a constructor to inject 'userDetailsService'
public class WebSecurityConfig {

    private UserDetailsServiceImpl userDetailsService;

    // 1. Define our custom JWT Filter bean.
    // This filter will intercept every request to check for a valid token.
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(){
        return new JwtAuthenticationFilter();
    }

    // 2. Define how to "hash" passwords.
    // We use BCrypt so we never store plain-text passwords in the database.
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    // 3. The AuthenticationManager is the main "engine" that processes login requests.
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // 4. The Provider connects our UserDetailsService (database) with our PasswordEncoder.
    // It tells Spring: "Use this service to find users and this encoder to check their password."
    @Bean
    public DaoAuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    // 5. The SecurityFilterChain is the actual "Gatekeeper."
    // It defines the rules for every incoming HTTP request.
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF because JWT-based APIs are stateless and don't use session cookies
                .csrf(AbstractHttpConfigurer::disable)

                // Define URL permissions
                .authorizeHttpRequests(auth -> auth
                        // Allow browsers to send "Pre-flight" OPTIONS requests without a token
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Public: Anyone can register or login
                        .requestMatchers("/api/auth/**").permitAll()

                        // Private: Only logged-in users can create/manage URLs
                        .requestMatchers("/api/urls/**").authenticated()

                        // Public: Anyone can use a shortened link (e.g., yoursite.com/xyz)
                        .requestMatchers("/{shortUrl}").permitAll()

                        // Everything else requires a login
                        .anyRequest().authenticated()
                );

        // Tell Spring to use our specific user/password logic
        http.authenticationProvider(authenticationProvider());

        // CRITICAL: Insert our JWT filter BEFORE the standard UsernamePassword filter.
        // This ensures we check for a token before Spring tries to look for a session.
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}