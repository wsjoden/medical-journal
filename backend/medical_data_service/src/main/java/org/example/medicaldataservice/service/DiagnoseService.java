package org.example.medicaldataservice.service;

import org.example.medicaldataservice.dto.DiagnoseDTO;
import org.example.medicaldataservice.dto.StaffDTO;
import org.example.medicaldataservice.model.Diagnose;
import org.example.medicaldataservice.repository.IDiagnoseRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DiagnoseService {

    private final IDiagnoseRepository diagnoseRepository;
    private final WebClient.Builder webClientBuilder;

    @Value("${user.service.url}")
    private String userServiceURL;

    public DiagnoseService(IDiagnoseRepository diagnoseRepository, WebClient.Builder webClientBuilder) {
        this.diagnoseRepository = diagnoseRepository;
        this.webClientBuilder = webClientBuilder;
    }

    public void registerDiagnose(Diagnose diagnose) {
        diagnoseRepository.save(diagnose);
    }

    public List<Diagnose> findDiagnosesByPatientUserId(String patientUserId) {
        return diagnoseRepository.findByPatientUserId(patientUserId);
    }

    public void createDiagnose(String patientUserId, String staffUserId, Diagnose diagnose) {
        diagnose.setPatientUserId(patientUserId);
        diagnose.setStaffUserId(staffUserId);
        diagnose.setDiagnosisDate(LocalDate.now());
        registerDiagnose(diagnose);
    }

    public List<DiagnoseDTO> getPatientDiagnoses(String patientUserId) {
        List<Diagnose> diagnoses = findDiagnosesByPatientUserId(patientUserId);

        if (diagnoses == null || diagnoses.isEmpty()) {
            return Collections.emptyList();
        }

        return diagnoses.stream()
                .map(this::convertToDiagnoseDTO)
                .collect(Collectors.toList());
    }

    private DiagnoseDTO convertToDiagnoseDTO(Diagnose diagnose) {
        return new DiagnoseDTO(
                diagnose.getId(),
                diagnose.getPatientUserId(),
                diagnose.getStaffUserId(),
                diagnose.getDiagnose(),
                diagnose.getDetails(),
                diagnose.getDiagnosisDate());
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
