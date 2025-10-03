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

    public ObservationController(ObservationService observationService) {
        this.observationService = observationService;
    }

    @PostMapping("/new/patient/{id}")
    public ResponseEntity<Void> registerObservation(Authentication authentication,
            @RequestBody Observation observation,
            @PathVariable String id) {

        String staffUserId = authentication.getName();
        if (staffUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        observationService.createObservation(id, staffUserId, observation);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/patient/{id}")
    public ResponseEntity<List<ObservationDTO>> findObservationByPatientId(@PathVariable String id) {
        List<ObservationDTO> observations = observationService.getPatientObservations(id);
        if (observations.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(observations);
    }

}
