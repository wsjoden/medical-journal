package org.example.medicaldataservice.repository;

import org.example.medicaldataservice.model.Diagnose;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IDiagnoseRepository extends JpaRepository<Diagnose, Long> {

    // Find diagnoses by patient ID
    List<Diagnose> findByPatientUserId(String patientUserId);
}
