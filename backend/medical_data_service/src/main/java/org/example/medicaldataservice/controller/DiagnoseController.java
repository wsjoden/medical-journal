package org.example.medicaldataservice.controller;

import org.apache.coyote.Response;
import org.example.medicaldataservice.dto.DiagnoseDTO;
import org.example.medicaldataservice.dto.JwtDTO;
import org.example.medicaldataservice.dto.StaffDTO;
import org.example.medicaldataservice.model.Diagnose;
import org.example.medicaldataservice.service.DiagnoseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/diagnoses")
public class DiagnoseController {

    @Autowired
    private final DiagnoseService diagnoseService;

    public DiagnoseController(DiagnoseService diagnoseService) {
        this.diagnoseService = diagnoseService;
    }

    @GetMapping("/test")
    public String test() {
        return "Medical service is up and running!";
    }

    @PostMapping("/new/patient/{id}")
    public ResponseEntity<Response> registerDiagnose(Authentication authentication,
            @RequestBody Diagnose diagnose,
            @PathVariable String id) {
        String staffUserId = authentication.getName();

        if (staffUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        diagnoseService.createDiagnose(id, staffUserId, diagnose);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/patient/{id}")
    public ResponseEntity<List<DiagnoseDTO>> getDiagnosesByPatientId(@PathVariable String id) {
        List<DiagnoseDTO> diagnoses = diagnoseService.getPatientDiagnoses(id);
        if (diagnoses.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.ok(diagnoses);

    }
}
