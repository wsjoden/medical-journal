package org.example.medicaldataservice.service;

import org.example.medicaldataservice.dto.EncounterDTO;
import org.example.medicaldataservice.dto.StaffDTO;
import org.example.medicaldataservice.model.Encounter;
import org.example.medicaldataservice.repository.IEncounterRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EncounterService {

    private final IEncounterRepository encounterRepository;
    private final WebClient.Builder webClientBuilder;

    @Value("${user.service.url}")
    private String userServiceURL;

    public EncounterService(IEncounterRepository encounterRepository, WebClient.Builder webClientBuilder) {
        this.encounterRepository = encounterRepository;
        this.webClientBuilder = webClientBuilder;
    }

    public void registerEncounter(Encounter encounter) {
        encounterRepository.save(encounter);
    }

    public Encounter findEncounterById(Long id) {
        return encounterRepository.findById(id).orElse(null);
    }

    public List<Encounter> findEncountersByPatientUserId(String patientUserId) {
        return encounterRepository.findByPatientUserId(patientUserId);
    }

    public Encounter createEncounter(String patientUserId, String staffUserId, Encounter encounter) {
        encounter.setPatientUserId(patientUserId);
        encounter.setStaffUserId(staffUserId);
        registerEncounter(encounter);
        return encounter;
    }

    public List<EncounterDTO> getPatientEncounters(String patientUserId) {
        List<Encounter> encounters = findEncountersByPatientUserId(patientUserId);

        if (encounters == null || encounters.isEmpty()) {
            return Collections.emptyList();
        }

        return encounters.stream()
                .map(this::convertToEncounterDTO)
                .collect(Collectors.toList());
    }

    private EncounterDTO convertToEncounterDTO(Encounter encounter) {
        return new EncounterDTO(
                encounter.getId(),
                encounter.getPatientUserId(),
                encounter.getStaffUserId(),
                encounter.getNotes(),
                encounter.getEncounterDate());
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
