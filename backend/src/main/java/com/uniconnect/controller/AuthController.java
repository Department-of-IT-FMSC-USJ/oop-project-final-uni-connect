package com.uniconnect.controller;

import com.uniconnect.config.JwtService;
import com.uniconnect.dto.LoginRequest;
import com.uniconnect.dto.LoginResponse;
import com.uniconnect.dto.RegisterRequest;
import com.uniconnect.model.Role;
import com.uniconnect.model.User;
import jakarta.validation.Valid;
import com.uniconnect.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(UserService userService, AuthenticationManager authenticationManager, JwtService jwtService) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            if (request.getPassword() == null || request.getConfirmPassword() == null
                    || !request.getPassword().equals(request.getConfirmPassword())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("success", false, "message", "Passwords do not match"));
            }

            if (request.getRole() != null && request.getRole() != Role.UNDERGRADUATE) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "Only undergraduates can self-register"));
            }

            request.setRole(Role.UNDERGRADUATE);
            User user = userService.registerUser(request);

            String token = jwtService.generateToken(user);
            LoginResponse loginResp = new LoginResponse(
                    token, user.getId(), user.getEmail(), user.getFullName(),
                    user.getRole(), user.getDepartment(), user.getProfilePicture());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("success", true, "message", "User registered successfully", "data", loginResp));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            User user = (User) userService.loadUserByUsername(request.getEmail());
            String token = jwtService.generateToken(user);

            LoginResponse loginResp = new LoginResponse(
                    token, user.getId(), user.getEmail(), user.getFullName(),
                    user.getRole(), user.getDepartment(), user.getProfilePicture());

            return ResponseEntity.ok(Map.of("success", true, "message", "Login successful", "data", loginResp));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Invalid credentials"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(Map.of("success", true, "message", "Logged out successfully"));
    }
}
