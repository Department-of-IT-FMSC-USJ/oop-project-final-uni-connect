package com.uniconnect.config;

import com.uniconnect.model.Role;
import com.uniconnect.model.User;
import com.uniconnect.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${HOD_EMAIL:hod@university.edu}")
    private String hodEmail;

    @Value("${HOD_PASSWORD:admin123}")
    private String hodPassword;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmail(hodEmail)) {
            User hod = User.builder()
                    .fullName("Dr. Department Head")
                    .email(hodEmail)
                    .password(passwordEncoder.encode(hodPassword))
                    .role(Role.DEPARTMENT_HEAD)
                    .department("Department of Information Technology")
                    .build();
            userRepository.save(hod);
            System.out.println("Default HoD account created: " + hodEmail);
        }
    }
}
