package org.example.medicaldataservice.repository;

import org.example.medicaldataservice.model.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface IEncounterRepository extends JpaRepository<Encounter, Long> {


    //Find all encounters for a patient
    List<Encounter> findByPatientUserId(String patientUserId);
    //Find all encounters between 2 dates
    List<Encounter> findByEncounterDateBetween(LocalDate startDate, LocalDate endDate);

}
