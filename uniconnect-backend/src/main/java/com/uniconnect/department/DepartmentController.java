package com.uniconnect.department;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @PostMapping("/validate-cpm")
    public ResponseEntity<?> validateCpmNumber(@RequestParam String department, @RequestParam String cpmNumber) {
        try {
            boolean isValid = departmentService.validateCpmNumber(department, cpmNumber);
            Map<String, Object> response = new HashMap<>();
            response.put("valid", isValid);
            
            if (!isValid) {
                response.put("message", "CPM number is not registered for this department");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            response.put("message", "CPM number is valid");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("valid", false);
            error.put("message", "Error validating CPM number: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllDepartments() {
        try {
            return ResponseEntity.ok(departmentService.getAllDepartments());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching departments");
        }
    }

    @PostMapping
    public ResponseEntity<?> createDepartment(@RequestBody Department department) {
        try {
            Department savedDepartment = departmentService.saveDepartment(department);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedDepartment);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error creating department: " + e.getMessage());
        }
    }

    @GetMapping("/{name}")
    public ResponseEntity<?> getDepartment(@PathVariable String name) {
        try {
            var department = departmentService.getDepartmentByName(name);
            if (department.isPresent()) {
                return ResponseEntity.ok(department.get());
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Department not found");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching department");
        }
    }
}
