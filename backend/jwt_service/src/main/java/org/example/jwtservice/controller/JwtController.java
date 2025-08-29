package org.example.jwtservice.controller;

import org.example.jwtservice.dto.JwtDTO;
import org.example.jwtservice.service.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/jwt")
public class JwtController {

    @Value("${keycloak.credentials.secret}")
    private String keycloakSecret;

    private final String keycloakUrl = "http://keycloak-service:8080/realms/patient_app/.well-known/openid-configuration";


    @GetMapping("/test")
    public String test() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject(keycloakUrl, String.class);
            System.out.println("response from keycloak" + response);
            return "JWT service is up and running! Keycloak is reachable: " + response;
        } catch (Exception e) {
            return "JWT service is up but failed to connect to Keycloak: " + e.getMessage();
        }
    }

    @PostMapping("/generate")
    public ResponseEntity<String> generateToken(@RequestBody JwtDTO jwtDTO) {
        System.out.println("generateToken() called");
        String keycloakEndpoint = "http://keycloak-service:8080/realms/patient_app/protocol/openid-connect/token";
        RestTemplate restTemplate = new RestTemplate();
        String clientId = "jwt-service";
        String clientSecret = keycloakSecret;

        String requestBody = String.format(
                "grant_type=password&client_id=%s&client_secret=%s&username=%s&password=%s",
                clientId, clientSecret, jwtDTO.getUsername(), jwtDTO.getPassword()
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(keycloakEndpoint, request, String.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Failed to generate token: " + e.getMessage());
        }
    }

    @GetMapping("/extract")
    public ResponseEntity<JwtDTO> extractToken(@AuthenticationPrincipal Jwt jwt) {
        try {
            // Extract claims from the JWT
            String username = jwt.getClaim("preferred_username");
            String role = jwt.getClaim("realm_access").get("roles").toString(); // Adjust if roles are nested
            Long userId = jwt.getClaim("userId");

            // Construct the DTO
            JwtDTO jwtDTO = new JwtDTO(username, role, userId);

            System.out.println("Extracted JWT DTO: " + jwtDTO);

            return ResponseEntity.ok(jwtDTO);
        } catch (Exception e) {
            System.err.println("Error extracting token: " + e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }
}
