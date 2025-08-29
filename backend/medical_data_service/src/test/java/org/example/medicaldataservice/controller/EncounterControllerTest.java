package org.example.medicaldataservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.medicaldataservice.dto.EncounterDTO;
import org.example.medicaldataservice.model.Diagnose;
import org.example.medicaldataservice.model.Encounter;
import org.example.medicaldataservice.service.EncounterService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class EncounterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EncounterService encounterService;

    @MockBean
    private WebClient.Builder webClientBuilder;

    @SpyBean
    private EncounterController encounterController;

    @Test
    public void testRegisterEncounter() throws Exception {
        String patientId = "patient1";
        String staffId = "doctor1";

        Encounter encounter = new Encounter();
        encounter.setNotes("mock encounter");
        encounter.setEncounterDate(LocalDate.now());

        JwtAuthenticationToken authentication = createMockAuthenticationToken(staffId, "Doctor");

        mockMvc.perform(post("/encounters/new/patient/{id}", patientId)
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(encounter)))
                .andExpect(status().isOk());

        verify(encounterService).registerEncounter(argThat(e ->
                e.getPatientUserId().equals(patientId) &&
                        e.getStaffUserId().equals(staffId) &&
                        e.getNotes().equals("mock encounter") &&
                        e.getEncounterDate() != null
        ));
    }

    @Test
    public void testRegisterEncounterWithoutAuthentication() throws Exception {
        // Create test data
        String patientId = "patient1";

        Encounter encounter = new Encounter();
        encounter.setNotes("mock encounter");
        encounter.setEncounterDate(LocalDate.now());

        mockMvc.perform(post("/encounters/new/patient/{id}", patientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(encounter)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testFindEncounterByPatientId() throws Exception {
        // Create test data
        String patientId = "patient1";
        String staffId = "doctor1";

        Encounter encounter1 = new Encounter();
        encounter1.setId(1L);
        encounter1.setPatientUserId(patientId);
        encounter1.setStaffUserId(staffId);
        encounter1.setNotes("mock encounter 1");
        encounter1.setEncounterDate(LocalDate.of(2023, 1, 15));

        Encounter encounter2 = new Encounter();
        encounter2.setId(2L);
        encounter2.setPatientUserId(patientId);
        encounter2.setStaffUserId(staffId);
        encounter2.setNotes("mock encounter 2");
        encounter2.setEncounterDate(LocalDate.of(2023, 2, 20));

        List<Encounter> encounters = Arrays.asList(encounter1, encounter2);

        when(encounterService.findEncountersByPatientUserId(patientId)).thenReturn(encounters);

        JwtAuthenticationToken authentication = createMockAuthenticationToken(staffId, "Doctor");

        mockMvc.perform(get("/encounters/patient/{id}", patientId)
                        .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].patientUserId").value(patientId))
                .andExpect(jsonPath("$[0].staffUserId").value(staffId))
                .andExpect(jsonPath("$[0].notes").value("mock encounter 1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].patientUserId").value(patientId))
                .andExpect(jsonPath("$[1].staffUserId").value(staffId))
                .andExpect(jsonPath("$[1].notes").value("mock encounter 2"));
    }

    @Test
    public void testFindEncounterByPatientIdNotFound() throws Exception {
        String patientId = "nonexistent";

        when(encounterService.findEncountersByPatientUserId(patientId)).thenReturn(Collections.emptyList());

        JwtAuthenticationToken authentication = createMockAuthenticationToken("doctor1", "Doctor");

        mockMvc.perform(get("/encounters/patient/{id}", patientId)
                        .with(authentication(authentication)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testFindEncounterByPatientIdWithoutAuthentication() throws Exception {
        String patientId = "patient1";

        mockMvc.perform(get("/encounters/patient/{id}", patientId))
                .andExpect(status().isUnauthorized());
    }

    private JwtAuthenticationToken createMockAuthenticationToken(String userId, String role) {
        Map<String, Object> headers = new HashMap<>();
        headers.put("alg", "RS256");

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", userId);
        claims.put("iss", "localhost:8080/");
        claims.put("role", role);

        Jwt jwt = new Jwt(
                "token-value-for-testing",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                headers,
                claims
        );

        return new JwtAuthenticationToken(
                jwt,
                Collections.singletonList(new SimpleGrantedAuthority(role)),
                userId
        );
    }
}