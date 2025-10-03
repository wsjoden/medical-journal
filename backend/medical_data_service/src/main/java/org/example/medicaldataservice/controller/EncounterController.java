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
    private final EncounterService encounterService;

    public EncounterController(EncounterService encounterService) {
        this.encounterService = encounterService;
    }

    @PostMapping("/new/patient/{id}")
    public ResponseEntity<Encounter> registerEncounter(Authentication authentication,
            @RequestBody Encounter encounter,
            @PathVariable String id) {

        String staffUserId = authentication.getName();
        if (staffUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        encounterService.createEncounter(id, staffUserId, encounter);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/patient/{id}")
    public ResponseEntity<List<EncounterDTO>> findEncounterByPatientId(@PathVariable String id) {
        List<EncounterDTO> encounters = encounterService.getPatientEncounters(id);
        if (encounters == null || encounters.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(encounters);
    }
}
