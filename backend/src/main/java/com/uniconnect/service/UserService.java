package com.uniconnect.service;

import com.uniconnect.academic.modules.allocations.service.StudentAllocationService;
import com.uniconnect.dto.CreateAccountRequest;
import com.uniconnect.dto.ProfileUpdateRequest;
import com.uniconnect.dto.RegisterRequest;
import com.uniconnect.model.Role;
import com.uniconnect.model.User;
import com.uniconnect.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final StudentAllocationService studentAllocationService;

    public UserService(UserRepository userRepository,
                        PasswordEncoder passwordEncoder,
                        StudentAllocationService studentAllocationService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.studentAllocationService = studentAllocationService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmailAndActiveTrue(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
    }

    public User registerUser(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        if (request.getRole() == Role.UNDERGRADUATE) {
            String email = request.getEmail() == null ? "" : request.getEmail().trim();
            String mcNumber = request.getRegistrationNumber() == null ? "" : request.getRegistrationNumber().trim();
            String cpmNumber = request.getCpmNumber() == null ? "" : request.getCpmNumber().trim();
            String profilePicture = request.getProfilePicture() == null ? "" : request.getProfilePicture().trim();
            String yearOfStudy = request.getYearOfStudy() == null ? "" : request.getYearOfStudy().trim();

            if (mcNumber.isEmpty() || cpmNumber.isEmpty() || profilePicture.isEmpty() || email.isEmpty()) {
                throw new IllegalArgumentException("MC number, CPM number, profile picture, and email are required for undergraduate registration");
            }

            // MC number: 6 digits, numeric only
            if (!mcNumber.matches("^\\d{6}$")) {
                throw new IllegalArgumentException("MC number must be exactly 6 digits (numeric only).");
            }

            // CPM number: 5 digits, numeric only
            if (!cpmNumber.matches("^\\d{5}$")) {
                throw new IllegalArgumentException("CPM number must be exactly 5 digits (numeric only).");
            }

            // Email format must be: <MC>@mgt.sjp.ac.lk
            String expectedEmail = mcNumber + "@mgt.sjp.ac.lk";
            if (!email.equalsIgnoreCase(expectedEmail)) {
                throw new IllegalArgumentException("Email must be exactly in the format " + expectedEmail + ".");
            }

            // Year of study must be 1..4
            if (!yearOfStudy.matches("^[1-4]$")) {
                throw new IllegalArgumentException("Year of study must be between 1 and 4.");
            }
        }

        String department = request.getDepartment();
        if (request.getRole() == Role.UNDERGRADUATE && (department == null || department.trim().isEmpty())) {
            department = "Department of Information Technology";
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .phone(request.getPhone())
                .department(department)
                .profilePicture(request.getProfilePicture())
                .registrationNumber(request.getRegistrationNumber())
                .cpmNumber(request.getCpmNumber())
                .yearOfStudy(request.getYearOfStudy())
                .build();

        User saved = userRepository.save(user);

        if (saved.getRole() == Role.UNDERGRADUATE && saved.getId() != null) {
            try {
                studentAllocationService.allocateStudentAuto(saved.getId().intValue());
            } catch (Exception ignored) {
                // If no mentors exist yet, the allocation will happen later
            }
        }

        return saved;
    }

    public User createAccount(User currentUser, CreateAccountRequest request) {
        if (request.getRole() == Role.UNDERGRADUATE) {
            throw new IllegalArgumentException("Use the registration endpoint for undergraduates");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        boolean currentIsHead = currentUser.getRole() == Role.DEPARTMENT_HEAD;
        boolean currentIsAssistant = currentUser.getRole() == Role.DEPARTMENT_ASSISTANT;
        if (!currentIsHead && !currentIsAssistant) {
            throw new IllegalArgumentException("Only department heads or department assistants can create accounts.");
        }

        if (request.getRole() == Role.DEPARTMENT_ASSISTANT) {
            if (!currentIsHead) {
                throw new IllegalArgumentException("Only department heads can create department assistants.");
            }
            long assistantCount = userRepository.countByRoleAndManagedByDepartmentHeadIdAndActiveTrue(Role.DEPARTMENT_ASSISTANT, currentUser.getId());
            if (assistantCount >= 2) {
                throw new IllegalArgumentException("A department head can only have up to 2 assistants.");
            }
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .phone(request.getPhone())
                .department(resolveDepartment(currentUser, request))
                .cpmNumber(request.getCpmNumber())
                .profilePicture(request.getProfilePicture())
                .managedByDepartmentHeadId(resolveManagingHeadId(currentUser, request.getRole()))
                .build();

        User created = userRepository.save(user);

        // If mentors are created later, allocate any still-unallocated undergraduates.
        if (created.getRole() == Role.ACADEMIC_MENTOR) {
            List<User> undergraduates = userRepository.findByRoleAndActiveTrue(Role.UNDERGRADUATE);
            for (User undergraduate : undergraduates) {
                if (undergraduate.getId() == null) {
                    continue;
                }
                try {
                    studentAllocationService.allocateStudentAuto(undergraduate.getId().intValue());
                } catch (Exception ignored) {
                    // Ignore already allocated / capacity / no matching mentor constraints.
                }
            }
        }

        return created;
    }

    private String resolveDepartment(User currentUser, CreateAccountRequest request) {
        if (request.getDepartment() != null && !request.getDepartment().trim().isEmpty()) {
            return request.getDepartment().trim();
        }
        return currentUser.getDepartment();
    }

    private Long resolveManagingHeadId(User currentUser, Role targetRole) {
        if (targetRole != Role.DEPARTMENT_ASSISTANT) {
            return null;
        }
        if (currentUser.getRole() == Role.DEPARTMENT_HEAD) {
            return currentUser.getId();
        }
        return currentUser.getManagedByDepartmentHeadId();
    }

    public User updateProfile(String email, ProfileUpdateRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setDepartment(request.getDepartment());
        user.setProfilePicture(request.getProfilePicture());
        user.setRegistrationNumber(request.getRegistrationNumber());
        user.setCpmNumber(request.getCpmNumber());
        user.setYearOfStudy(request.getYearOfStudy());

        return userRepository.save(user);
    }

    public List<User> getUsersByRole(Role role) {
        return userRepository.findByRoleAndActiveTrue(role);
    }

    public List<User> getDepartmentAssistants(User currentUser) {
        Long managerId = currentUser.getRole() == Role.DEPARTMENT_HEAD
                ? currentUser.getId()
                : currentUser.getManagedByDepartmentHeadId();
        if (managerId == null) {
            return List.of();
        }
        return userRepository.findByRoleAndManagedByDepartmentHeadIdAndActiveTrue(Role.DEPARTMENT_ASSISTANT, managerId);
    }

    public void deleteAccount(User currentUser, Long userId) {
        if (currentUser.getRole() != Role.DEPARTMENT_HEAD) {
            throw new IllegalArgumentException("Only department heads can delete accounts.");
        }

        User target = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        if (Boolean.FALSE.equals(target.getActive())) {
            return;
        }

        if (target.getRole() != Role.DEPARTMENT_ASSISTANT
                && target.getRole() != Role.ACADEMIC_MENTOR
                && target.getRole() != Role.INDUSTRY_MENTOR
                && target.getRole() != Role.UNDERGRADUATE) {
            throw new IllegalArgumentException("This account type cannot be deleted from the HOD workspace.");
        }

        if (target.getRole() == Role.DEPARTMENT_ASSISTANT
                && !currentUser.getId().equals(target.getManagedByDepartmentHeadId())) {
            throw new IllegalArgumentException("You can only delete assistants created under your department workspace.");
        }

        target.setActive(false);
        target.setEmail(buildDeletedEmail(target));
        userRepository.save(target);
    }

    private String buildDeletedEmail(User target) {
        String original = target.getEmail() == null ? "deleted@uniconnect.local" : target.getEmail().trim().toLowerCase();
        String local = original.contains("@") ? original.substring(0, original.indexOf('@')) : original;
        return "deleted+" + target.getId() + "+" + local + "@uniconnect.local";
    }

    public List<User> searchStudentsByMinGpa(String department) {
        return userRepository.findByRoleAndActiveTrue(Role.UNDERGRADUATE).stream()
                .filter(u -> department == null || department.isEmpty()
                        || (u.getDepartment() != null && u.getDepartment().equalsIgnoreCase(department)))
                .collect(Collectors.toList());
    }

    public List<User> getTopStudentsByPoints(int limit) {
        return userRepository.findByRoleAndActiveTrue(Role.UNDERGRADUATE).stream()
                .sorted((a, b) -> {
                    int pa = a.getCumulativePoints() == null ? 0 : a.getCumulativePoints();
                    int pb = b.getCumulativePoints() == null ? 0 : b.getCumulativePoints();
                    return Integer.compare(pb, pa);
                })
                .limit(limit)
                .collect(Collectors.toList());
    }
}
