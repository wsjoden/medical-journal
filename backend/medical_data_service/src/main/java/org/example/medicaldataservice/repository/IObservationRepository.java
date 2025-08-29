package org.example.medicaldataservice.repository;

import org.example.medicaldataservice.model.Observation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface IObservationRepository extends JpaRepository<Observation, Long> {

    // Find observations by patient ID
    List<Observation> findByPatientUserId(String patientUserId);

    // Find observations by doctor ID
    List<Observation> findByStaffUserId(String staffUserId);

    // Find observations by date
    List<Observation> findByObservationDate(LocalDate date);

    // Find observations by value
    List<Observation> findByObservation(String observation);
}
