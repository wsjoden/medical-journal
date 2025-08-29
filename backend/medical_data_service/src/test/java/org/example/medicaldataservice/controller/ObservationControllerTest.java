package org.example.medicaldataservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.medicaldataservice.dto.ObservationDTO;
import org.example.medicaldataservice.model.Observation;
import org.example.medicaldataservice.service.ObservationService;
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
public class ObservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ObservationService observationService;

    @MockBean
    private WebClient.Builder webClientBuilder;

    @SpyBean
    private ObservationController observationController;

    @Test
    public void testRegisterObservation() throws Exception {
        String patientId = "patient1";
        String staffId = "doctor1";

        Observation observation = new Observation();
        observation.setId(1L);
        observation.setObservation("mock observation");
        observation.setObservationDate(LocalDate.now());

        JwtAuthenticationToken authentication = createMockAuthenticationToken(staffId, "Doctor");

        mockMvc.perform(post("/observations/new/patient/{id}", patientId)
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(observation)))
                .andExpect(status().isOk());

        verify(observationService).registerObservation(argThat(o ->
                o.getPatientUserId().equals(patientId) &&
                        o.getStaffUserId().equals(staffId) &&
                        o.getObservation().equals("mock observation") &&
                        o.getObservationDate() != null
        ));
    }

    @Test
    public void testRegisterObservationWithoutAuthentication() throws Exception {
        String patientId = "patient1";

        Observation observation = new Observation();
        observation.setId(1L);
        observation.setObservation("mock observation");
        observation.setObservationDate(LocalDate.now());

        mockMvc.perform(post("/observations/new/patient/{id}", patientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(observation)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testFindObservationByPatientId() throws Exception {
        String patientId = "patient1";
        String staffId = "doctor1";

        Observation observation1 = new Observation();
        observation1.setId(1L);
        observation1.setPatientUserId(patientId);
        observation1.setStaffUserId(staffId);
        observation1.setObservation("mock observation 1");
        observation1.setObservationDate(LocalDate.of(2023, 1, 15));

        Observation observation2 = new Observation();
        observation2.setId(2L);
        observation2.setPatientUserId(patientId);
        observation2.setStaffUserId(staffId);
        observation2.setObservation("mock observation 2");
        observation2.setObservationDate(LocalDate.of(2023, 2, 20));

        List<Observation> observations = Arrays.asList(observation1, observation2);

        when(observationService.findObservationsByPatientUserId(patientId)).thenReturn(observations);

        JwtAuthenticationToken authentication = createMockAuthenticationToken(staffId, "Doctor");

        mockMvc.perform(get("/observations/patient/{id}", patientId)
                        .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].patientUserId").value(patientId))
                .andExpect(jsonPath("$[0].staffUserId").value(staffId))
                .andExpect(jsonPath("$[0].observation").value("mock observation 1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].patientUserId").value(patientId))
                .andExpect(jsonPath("$[1].staffUserId").value(staffId))
                .andExpect(jsonPath("$[1].observation").value("mock observation 2"));
    }

    @Test
    public void testFindObservationByPatientIdNotFound() throws Exception {
        String patientId = "nonexistent";

        when(observationService.findObservationsByPatientUserId(patientId)).thenReturn(Collections.emptyList());

        JwtAuthenticationToken authentication = createMockAuthenticationToken("doctor1", "Doctor");

        mockMvc.perform(get("/observations/patient/{id}", patientId)
                        .with(authentication(authentication)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testFindObservationByPatientIdWithoutAuthentication() throws Exception {
        String patientId = "patient1";

        mockMvc.perform(get("/observations/patient/{id}", patientId))
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