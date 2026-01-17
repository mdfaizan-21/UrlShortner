package com.url.shortner.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtils jwtTokenProvider;
    @Autowired
    private UserDetailsService userDetailsService;

    //In Spring Security, the OncePerRequestFilter acts as a security gatekeeper. It intercepts every incoming request to check for a valid "passport" (the JWT).
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // 1. Calls the helper method in JwtUtils to extract the token string from the "Authorization" header
            String jwt = jwtTokenProvider.getJwtFromHeader(request);

            // 2. Checks if the token exists and passes the cryptographic/expiration validation checks
            if (jwt != null && jwtTokenProvider.validateToken(jwt)) {

                // 3. Extracts the username (the 'subject') from the validated JWT payload
                String username = jwtTokenProvider.getUserNameFromJwtToken(jwt);

                // 4. Loads the full user details (password, roles, etc.) from the database using the username
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // 5. Creates an Authentication object—this represents the "logged-in" state in Spring Security.
                // We pass null for credentials because the user is already authenticated via the token.
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                // 6. Attaches extra web-related details (like the IP address or session ID) to the authentication object
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 7. Stores the authentication object into the SecurityContext.
                // This is crucial: it tells Spring "this user is officially logged in for the duration of this request."
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        } catch (Exception e) {
            // Logs any errors (like parsing issues or DB connection errors) to the console for debugging
            e.printStackTrace();
        }

        // 8. CRITICAL: Continues the request through the remaining filters in the chain.
        // Without this, the request would hang and never reach your Controller.
        filterChain.doFilter(request, response);
    }
}
