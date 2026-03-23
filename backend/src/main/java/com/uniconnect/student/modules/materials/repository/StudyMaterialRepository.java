package com.uniconnect.student.modules.materials.repository;

import com.uniconnect.student.modules.materials.entity.StudyMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for StudyMaterial entity.
 * Extends JpaRepository to provide CRUD operations.
 */
@Repository
public interface StudyMaterialRepository extends JpaRepository<StudyMaterial, Integer> {

    /**
     * Find all study materials uploaded by a specific student.
     *
     * @param uploadedBy the student ID
     * @return list of study materials
     */
    List<StudyMaterial> findByUploadedBy(Integer uploadedBy);

    /**
     * Find all study materials ordered by upload date descending.
     *
     * @return list of study materials
     */
    List<StudyMaterial> findAllByOrderByUploadDateDesc();
}
