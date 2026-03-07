package com.uniconnect.service;

import com.uniconnect.model.Department;
import com.uniconnect.repository.DepartmentRepository;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    public Optional<Department> getDepartmentByName(String name) {
        return departmentRepository.findByName(name);
    }

    public Optional<Department> getDepartmentByCode(String code) {
        return departmentRepository.findByCode(code);
    }

    public boolean validateCpmNumber(String departmentName, String cpmNumber) {
        Optional<Department> department = departmentRepository.findByName(departmentName);
        if (department.isPresent()) {
            return department.get().isCpmNumberValid(cpmNumber);
        }
        return false;
    }

    public Department saveDepartment(Department department) {
        return departmentRepository.save(department);
    }

    public Iterable<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }
}
