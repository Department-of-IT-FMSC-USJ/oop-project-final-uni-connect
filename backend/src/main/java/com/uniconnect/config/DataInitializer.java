package com.uniconnect.config;

import com.uniconnect.model.Role;
import com.uniconnect.model.User;
import com.uniconnect.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmail("hod@university.edu")) {
            User hod = User.builder()
                    .fullName("Dr. Department Head")
                    .email("hod@university.edu")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.DEPARTMENT_HEAD)
                    .department("Department of Information Technology")
                    .build();
            userRepository.save(hod);
            System.out.println("Default HoD account created: hod@university.edu / admin123");
        }
    }
}
