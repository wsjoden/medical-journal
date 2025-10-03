package org.example.medicaldataservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.medicaldataservice.dto.ObservationDTO;
import org.example.medicaldataservice.model.Observation;
import org.example.medicaldataservice.service.ObservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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

        @BeforeEach
        public void setup() {
                reset(observationService);
        }

        @Test
        public void testRegisterObservation() throws Exception {
                String patientId = "patient1";
                String staffId = "doctor1";

                Observation observation = new Observation();
                observation.setObservation("mock observation");
                observation.setObservationDate(LocalDate.now());

                Observation createdObservation = new Observation();
                createdObservation.setId(1L);
                createdObservation.setPatientUserId(patientId);
                createdObservation.setStaffUserId(staffId);
                createdObservation.setObservation("mock observation");
                createdObservation.setObservationDate(LocalDate.now());

                when(observationService.createObservation(eq(patientId), eq(staffId), any(Observation.class)))
                                .thenReturn(createdObservation);

                JwtAuthenticationToken authentication = createMockAuthenticationToken(staffId, "ROLE_Doctor");

                mockMvc.perform(post("/observations/new/patient/{id}", patientId)
                                .with(authentication(authentication))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(observation)))
                                .andExpect(status().isOk());

                verify(observationService).createObservation(
                                eq(patientId),
                                eq(staffId),
                                argThat(o -> "mock observation".equals(o.getObservation())));
        }

        @Test
        public void testRegisterObservationWithoutAuthentication() throws Exception {
                String patientId = "patient1";

                Observation observation = new Observation();
                observation.setObservation("mock observation");
                observation.setObservationDate(LocalDate.now());

                mockMvc.perform(post("/observations/new/patient/{id}", patientId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(observation)))
                                .andExpect(status().isUnauthorized());

                verify(observationService, never()).createObservation(anyString(), anyString(), any(Observation.class));
        }

        @Test
        public void testFindObservationByPatientId() throws Exception {
                String patientId = "patient1";
                String staffId = "doctor1";

                ObservationDTO observationDTO1 = new ObservationDTO(
                                1L,
                                patientId,
                                staffId,
                                "mock observation 1",
                                LocalDate.of(2023, 1, 15));

                ObservationDTO observationDTO2 = new ObservationDTO(
                                2L,
                                patientId,
                                staffId,
                                "mock observation 2",
                                LocalDate.of(2023, 2, 20));

                List<ObservationDTO> observationsDTO = Arrays.asList(observationDTO1, observationDTO2);

                when(observationService.getPatientObservations(patientId)).thenReturn(observationsDTO);

                mockMvc.perform(get("/observations/patient/{id}", patientId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].id").value(1))
                                .andExpect(jsonPath("$[0].patientUserId").value(patientId))
                                .andExpect(jsonPath("$[0].staffUserId").value(staffId))
                                .andExpect(jsonPath("$[0].observation").value("mock observation 1"))
                                .andExpect(jsonPath("$[1].id").value(2))
                                .andExpect(jsonPath("$[1].patientUserId").value(patientId))
                                .andExpect(jsonPath("$[1].staffUserId").value(staffId))
                                .andExpect(jsonPath("$[1].observation").value("mock observation 2"));

                verify(observationService).getPatientObservations(patientId);
        }

        @Test
        public void testFindObservationByPatientIdNotFound() throws Exception {
                String patientId = "nonexistent";

                when(observationService.getPatientObservations(patientId)).thenReturn(Collections.emptyList());

                mockMvc.perform(get("/observations/patient/{id}", patientId))
                                .andExpect(status().isNotFound());

                verify(observationService).getPatientObservations(patientId);
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
                                claims);

                return new JwtAuthenticationToken(
                                jwt,
                                Collections.singletonList(new SimpleGrantedAuthority(role)),
                                userId);
        }
}