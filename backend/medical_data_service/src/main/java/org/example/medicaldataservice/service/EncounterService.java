package org.example.medicaldataservice.service;

import org.example.medicaldataservice.model.Encounter;
import org.example.medicaldataservice.repository.IEncounterRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EncounterService {

    private final IEncounterRepository encounterRepository;

    public EncounterService(IEncounterRepository encounterRepository) {
        this.encounterRepository = encounterRepository;
    }

    // Register an encounter
    public void registerEncounter(Encounter encounter) {
        encounterRepository.save(encounter);
    }

    // Find an encounter by id
    public Encounter findEncounterById(Long id) {
        return encounterRepository.findById(id).orElse(null);
    }

    // Find all encounters for specific Patient
    public List<Encounter> findEncountersByPatientUserId(String patientUserId) {
        return encounterRepository.findByPatientUserId(patientUserId);
    }

    // Remove encounter
    public void removeEncounter(Long id) {
        encounterRepository.deleteById(id);
    }

}
