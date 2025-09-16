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
    private final WebClient.Builder webClientBuilder;
    private final DiagnoseService diagnoseService;

    @Value("${user.service.url}")
    private String userServiceURL;

    public DiagnoseController(WebClient.Builder webClientBuilder, DiagnoseService diagnoseService) {
        this.webClientBuilder = webClientBuilder;
        this.diagnoseService = diagnoseService;
    }

    @GetMapping("/test")
    public String test() {
        System.out.println("Medical service is up and running!");
        return "Medical service is up and running!";
    }

    @PostMapping("/new/patient/{id}")
    public ResponseEntity<Response> registerDiagnose(Authentication authentication,
            @RequestBody Diagnose diagnose,
            @PathVariable String id) {
        String staffUserId = authentication.getName();

        if (staffUserId == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        diagnose.setPatientUserId(id);
        diagnose.setDiagnosisDate(LocalDate.now());

        diagnose.setStaffUserId(staffUserId);
        diagnoseService.registerDiagnose(diagnose);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/patient/{id}")
    public ResponseEntity<List<DiagnoseDTO>> getDiagnosesByPatientId(@PathVariable String id) {
        List<Diagnose> diagnoses = diagnoseService.findDiagnosesByPatientUserId(id);
        if (diagnoses == null || diagnoses.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            List<DiagnoseDTO> diagnosesDTO = diagnoses.stream()
                    .map(diagnose -> new DiagnoseDTO(
                            diagnose.getId(),
                            diagnose.getPatientUserId(),
                            diagnose.getStaffUserId(),
                            diagnose.getDiagnose(),
                            diagnose.getDetails(),
                            diagnose.getDiagnosisDate()))
                    .toList();
            return ResponseEntity.ok(diagnosesDTO);
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
