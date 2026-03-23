package com.uniconnect.config;

import com.uniconnect.model.User;
import com.uniconnect.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final UserService userService;

    public JwtAuthenticationFilter(UserService userService) {
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // Never block authentication endpoints based on an Authorization header.
        // The login/register endpoints must remain accessible even if a stale token exists in localStorage.
        String path = request.getRequestURI();
        if (path != null && path.startsWith("/api/auth/")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        final String tokenPrefix = "Bearer mock_token_for_";

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeaderValue = authHeader.substring(7); // "Bearer " is 7 chars

        if (authHeaderValue.startsWith("mock_token_for_")) {
            String userEmail = authHeaderValue.substring("mock_token_for_".length());

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                try {
                    User user = (User) userService.loadUserByUsername(userEmail);

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            user.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } catch (Exception ignored) {
                    // If the token is stale/invalid, just keep the request unauthenticated.
                    // This avoids hard-blocking public endpoints or causing confusing 403s.
                    SecurityContextHolder.clearContext();
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
