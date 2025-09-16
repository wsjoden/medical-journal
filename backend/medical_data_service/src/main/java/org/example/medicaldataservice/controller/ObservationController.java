package org.example.medicaldataservice.controller;

import org.apache.coyote.Response;
import org.example.medicaldataservice.dto.JwtDTO;
import org.example.medicaldataservice.dto.ObservationDTO;
import org.example.medicaldataservice.dto.StaffDTO;
import org.example.medicaldataservice.model.Observation;
import org.example.medicaldataservice.service.ObservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@RestController
@RequestMapping("/observations")
public class ObservationController {

    @Autowired
    private final ObservationService observationService;
    private final WebClient.Builder webClientBuilder;

    @Value("${user.service.url}")
    private String userServiceURL;

    public ObservationController(ObservationService observationService, WebClient.Builder webClientBuilder) {
        this.observationService = observationService;
        this.webClientBuilder = webClientBuilder;
    }

    @PostMapping("/new/patient/{id}")
    public ResponseEntity<Response> registerObservation(Authentication authentication,
            @RequestBody Observation observation,
            @PathVariable String id) {
        String staffUserId = authentication.getName();
        if (staffUserId == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        observation.setPatientUserId(id);

        observation.setStaffUserId(staffUserId);
        observationService.registerObservation(observation);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/patient/{id}")
    public ResponseEntity<List<ObservationDTO>> findObservationByPatientId(@PathVariable String id) {
        List<Observation> observations = observationService.findObservationsByPatientUserId(id);
        if (observations == null || observations.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            List<ObservationDTO> observationsDTO = observations.stream()
                    .map(observation -> new ObservationDTO(
                            observation.getId(),
                            observation.getPatientUserId(),
                            observation.getStaffUserId(),
                            observation.getObservation(),
                            observation.getObservationDate()))
                    .toList();
            return ResponseEntity.ok(observationsDTO);
        }
    }

    private StaffDTO getStaff(Long userId) {
        // String staffServiceURL = "http://user-service:8082/staff/" + userId;
        String url = userServiceURL + "/staff/" + userId;
        return this.webClientBuilder.build()
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(StaffDTO.class)
                .block();
    }
}
