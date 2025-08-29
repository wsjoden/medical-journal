package org.example.medicaldataservice.service;

import org.example.medicaldataservice.model.Observation;
import org.example.medicaldataservice.repository.IObservationRepository;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class ObservationService {
    private final IObservationRepository observationRepository;

    public ObservationService(IObservationRepository observationRepository) {
        this.observationRepository = observationRepository;
    }

    public void registerObservation(Observation observation) {
        observationRepository.save(observation);
    }

    // find all observations for a patient
    public List<Observation> findObservationsByPatientUserId(String patientUserId) {
        return observationRepository.findByPatientUserId(patientUserId);
    }

}
