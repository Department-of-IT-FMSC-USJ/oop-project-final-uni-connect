package com.uniconnect.repository;

import com.uniconnect.model.User;
import com.uniconnect.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailAndActiveTrue(String email);
    boolean existsByEmail(String email);
    List<User> findByMentorEligibleTrue();
    List<User> findByRole(Role role);
    List<User> findByRoleAndActiveTrue(Role role);
    List<User> findByRoleAndManagedByDepartmentHeadId(Role role, Long managedByDepartmentHeadId);
    List<User> findByRoleAndManagedByDepartmentHeadIdAndActiveTrue(Role role, Long managedByDepartmentHeadId);
    long countByRoleAndManagedByDepartmentHeadId(Role role, Long managedByDepartmentHeadId);
    long countByRoleAndManagedByDepartmentHeadIdAndActiveTrue(Role role, Long managedByDepartmentHeadId);
    Optional<User> findByRegistrationNumber(String registrationNumber);
}
