package com.uniconnect.service;

import com.uniconnect.dto.ProfileUpdateRequest;
import com.uniconnect.dto.RegisterRequest;
import com.uniconnect.model.User;
import com.uniconnect.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
    }

    public User registerUser(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .phone(request.getPhone())
                .department(request.getDepartment())
                .profilePicture(request.getProfilePicture())
                .registrationNumber(request.getRegistrationNumber())
                .cpmNumber(request.getCpmNumber())
                .yearOfStudy(request.getYearOfStudy())
                .build();

        return userRepository.save(user);
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
}
