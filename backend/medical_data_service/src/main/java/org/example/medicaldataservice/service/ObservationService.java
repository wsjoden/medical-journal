package org.example.medicaldataservice.service;

import org.example.medicaldataservice.dto.ObservationDTO;
import org.example.medicaldataservice.dto.StaffDTO;
import org.example.medicaldataservice.model.Observation;
import org.example.medicaldataservice.repository.IObservationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ObservationService {
    private final IObservationRepository observationRepository;
    private final WebClient.Builder webClientBuilder;

    @Value("${user.service.url}")
    private String userServiceURL;

    public ObservationService(IObservationRepository observationRepository, WebClient.Builder webClientBuilder) {
        this.observationRepository = observationRepository;
        this.webClientBuilder = webClientBuilder;
    }

    public void registerObservation(Observation observation) {
        observationRepository.save(observation);
    }

    // find all observations for a patient
    public List<Observation> findObservationsByPatientUserId(String patientUserId) {
        return observationRepository.findByPatientUserId(patientUserId);
    }

    public Observation createObservation(String patientUserId, String staffUserId, Observation observation) {
        observation.setPatientUserId(patientUserId);
        observation.setStaffUserId(staffUserId);
        registerObservation(observation);
        return observation;
    }

    public List<ObservationDTO> getPatientObservations(String patientUserId) {
        List<Observation> observations = findObservationsByPatientUserId(patientUserId);

        if (observations == null || observations.isEmpty()) {
            return Collections.emptyList();
        }

        return observations.stream()
                .map(this::convertToObservationDTO)
                .collect(Collectors.toList());
    }

    private ObservationDTO convertToObservationDTO(Observation observation) {
        return new ObservationDTO(
                observation.getId(),
                observation.getPatientUserId(),
                observation.getStaffUserId(),
                observation.getObservation(),
                observation.getObservationDate());
    }

    public StaffDTO getStaffById(Long userId) {
        String url = userServiceURL + "/staff/" + userId;
        return this.webClientBuilder.build()
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(StaffDTO.class)
                .block();
    }

}
