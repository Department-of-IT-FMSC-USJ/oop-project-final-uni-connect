package com.uniconnect.controller;

import com.uniconnect.dto.CreateAccountRequest;
import com.uniconnect.dto.ProfileUpdateRequest;
import com.uniconnect.model.Role;
import com.uniconnect.model.User;
import com.uniconnect.repository.UserRepository;
import com.uniconnect.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    public UserController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @GetMapping("/profile")
    public ResponseEntity<User> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        User user = (User) userService.loadUserByUsername(userDetails.getUsername());
        return ResponseEntity.ok(user);
    }

    @PutMapping("/profile")
    public ResponseEntity<User> updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                                              @RequestBody ProfileUpdateRequest request) {
        User updatedUser = userService.updateProfile(userDetails.getUsername(), request);
        return ResponseEntity.ok(updatedUser);
    }

    @PostMapping("/create-account")
    public ResponseEntity<?> createAccount(@AuthenticationPrincipal User currentUser,
                                           @Valid @RequestBody CreateAccountRequest request) {
        if (currentUser.getRole() != Role.DEPARTMENT_HEAD && currentUser.getRole() != Role.DEPARTMENT_ASSISTANT) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "message", "Only department heads or department assistants can create accounts"));
        }

        if (request.getRole() != Role.ACADEMIC_MENTOR
                && request.getRole() != Role.INDUSTRY_MENTOR
                && request.getRole() != Role.DEPARTMENT_ASSISTANT) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Can only create ACADEMIC_MENTOR, INDUSTRY_MENTOR, or DEPARTMENT_ASSISTANT accounts"));
        }

        try {
            User created = userService.createAccount(currentUser, request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("success", true, "message", "Account created successfully", "data", created));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/hod-assistants")
    public ResponseEntity<?> getDepartmentAssistants(@AuthenticationPrincipal User currentUser) {
        if (currentUser.getRole() != Role.DEPARTMENT_HEAD && currentUser.getRole() != Role.DEPARTMENT_ASSISTANT) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "message", "Only department heads or assistants can view department assistants"));
        }

        List<User> assistants = userService.getDepartmentAssistants(currentUser);
        return ResponseEntity.ok(Map.of("success", true, "data", assistants));
    }

    @GetMapping("/by-role/{role}")
    public ResponseEntity<?> getUsersByRole(@AuthenticationPrincipal User currentUser,
                                            @PathVariable String role) {
        if (currentUser.getRole() != Role.DEPARTMENT_HEAD
                && currentUser.getRole() != Role.DEPARTMENT_ASSISTANT
                && currentUser.getRole() != Role.ACADEMIC_MENTOR
                && currentUser.getRole() != Role.INDUSTRY_MENTOR) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "message", "Not authorized"));
        }

        try {
            Role targetRole = Role.valueOf(role.toUpperCase());
            List<User> users = userService.getUsersByRole(targetRole);
            return ResponseEntity.ok(Map.of("success", true, "data", users));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Invalid role: " + role));
        }
    }

    @GetMapping("/students/search")
    public ResponseEntity<?> searchStudents(@AuthenticationPrincipal User currentUser,
                                            @RequestParam(required = false) String department,
                                            @RequestParam(required = false) String query) {
        if (currentUser.getRole() != Role.DEPARTMENT_HEAD && currentUser.getRole() != Role.DEPARTMENT_ASSISTANT) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "message", "Only department heads or assistants can search students"));
        }

        List<User> students = userService.searchStudentsByMinGpa(department);
        if (query != null && !query.trim().isEmpty()) {
            String q = query.trim().toLowerCase();
            students = students.stream()
                    .filter(s -> (s.getFullName() != null && s.getFullName().toLowerCase().contains(q))
                            || (s.getRegistrationNumber() != null && s.getRegistrationNumber().toLowerCase().contains(q))
                            || (s.getEmail() != null && s.getEmail().toLowerCase().contains(q)))
                    .toList();
        }

        return ResponseEntity.ok(Map.of("success", true, "data", students));
    }

    @GetMapping("/students/top-points")
    public ResponseEntity<?> getTopStudents(@AuthenticationPrincipal User currentUser,
                                            @RequestParam(defaultValue = "10") int limit) {
        if (currentUser.getRole() != Role.DEPARTMENT_HEAD && currentUser.getRole() != Role.DEPARTMENT_ASSISTANT) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "message", "Only department heads or assistants can view top students"));
        }

        List<User> students = userService.getTopStudentsByPoints(limit);
        return ResponseEntity.ok(Map.of("success", true, "data", students));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@AuthenticationPrincipal User currentUser,
                                        @PathVariable Long userId) {
        try {
            userService.deleteAccount(currentUser, userId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Account deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserById(@AuthenticationPrincipal User currentUser,
                                         @PathVariable Long userId) {
        return userRepository.findById(userId)
                .filter(user -> Boolean.TRUE.equals(user.getActive()))
                .map(user -> ResponseEntity.ok(Map.of("success", true, "data", user)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "User not found")));
    }
}
