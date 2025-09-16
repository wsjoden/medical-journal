package org.example.medicaldataservice.controller;

import org.example.medicaldataservice.dto.DiagnoseDTO;
import org.example.medicaldataservice.dto.EncounterDTO;
import org.example.medicaldataservice.dto.JwtDTO;
import org.example.medicaldataservice.dto.StaffDTO;
import org.example.medicaldataservice.model.Diagnose;
import org.example.medicaldataservice.model.Encounter;
import org.example.medicaldataservice.service.EncounterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/encounters")
public class EncounterController {

    @Autowired
    private final WebClient.Builder webClientBuilder;
    private final EncounterService encounterService;

    @Value("${user.service.url}")
    private String userServiceURL;

    public EncounterController(EncounterService encounterService, WebClient.Builder webClientBuilder) {
        this.encounterService = encounterService;
        this.webClientBuilder = webClientBuilder;
    }

    // Create encounter
    @PostMapping("/new/patient/{id}")
    public ResponseEntity<Encounter> registerEncounter(Authentication authentication,
            @RequestBody Encounter encounter,
            @PathVariable String id) {
        String staffUserId = authentication.getName();
        if (staffUserId == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        encounter.setPatientUserId(id);
        System.out.println("staffId: " + staffUserId);

        encounter.setStaffUserId(staffUserId);
        System.out.println("encounter: " + encounter.toString());
        encounterService.registerEncounter(encounter);
        return ResponseEntity.ok(encounter);
    }

    @GetMapping("/patient/{id}")
    public ResponseEntity<List<EncounterDTO>> findEncounterByPatientId(@PathVariable String id) {
        List<Encounter> encounters = encounterService.findEncountersByPatientUserId(id);
        if (encounters == null || encounters.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            List<EncounterDTO> encountersDTO = encounters.stream()
                    .map(encounter -> new EncounterDTO(
                            encounter.getId(),
                            encounter.getPatientUserId(),
                            encounter.getStaffUserId(),
                            encounter.getNotes(),
                            encounter.getEncounterDate()))
                    .toList();
            return ResponseEntity.ok(encountersDTO);
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
