package com.wovengold.pdi.repository;

import com.wovengold.pdi.model.PDISubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PDISubmissionRepository extends JpaRepository<PDISubmission, Long> {
    
    @Query("SELECT DISTINCT p FROM PDISubmission p LEFT JOIN FETCH p.imageUrls WHERE p.id = :id")
    Optional<PDISubmission> findByIdWithImageUrls(@Param("id") Long id);
    
    @Query("SELECT DISTINCT p FROM PDISubmission p LEFT JOIN FETCH p.imageUrls")
    List<PDISubmission> findAllWithImageUrls();
} 