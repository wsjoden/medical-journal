package org.example.medicaldataservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.medicaldataservice.dto.DiagnoseDTO;
import org.example.medicaldataservice.model.Diagnose;
import org.example.medicaldataservice.service.DiagnoseService;
import org.junit.jupiter.api.BeforeEach;
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
public class DiagnoseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DiagnoseService diagnoseService;

    @MockBean
    private WebClient.Builder webClientBuilder;

    @SpyBean
    private DiagnoseController diagnoseController;

    @Test
    public void testTest() throws Exception {
        JwtAuthenticationToken authentication = createMockAuthenticationToken("doctor1", "Doctor");

        mockMvc.perform(get("/diagnoses/test")
                        .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(content().string("Medical service is up and running!"));
    }

    @Test
    public void testRegisterDiagnose() throws Exception {
        String patientId = "patient1";
        String staffId = "doctor1";

        Diagnose diagnose = new Diagnose();
        diagnose.setDiagnose("mock diagnose");
        diagnose.setDetails("mock details");

        JwtAuthenticationToken authentication = createMockAuthenticationToken(staffId, "Doctor");

        mockMvc.perform(post("/diagnoses/new/patient/{id}", patientId)
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(diagnose)))
                .andExpect(status().isOk());

        verify(diagnoseService).registerDiagnose(argThat(d ->
                d.getPatientUserId().equals(patientId) &&
                        d.getStaffUserId().equals(staffId) &&
                        d.getDiagnose().equals("mock diagnose") &&
                        d.getDetails().equals("mock details") &&
                        d.getDiagnosisDate() != null
        ));
    }

    @Test
    public void testRegisterDiagnoseWithoutAuthentication() throws Exception {
        String patientId = "patient1";

        Diagnose diagnose = new Diagnose();
        diagnose.setDiagnose("mock diagnose");
        diagnose.setDetails("mock details");

        mockMvc.perform(post("/diagnoses/new/patient/{id}", patientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(diagnose)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testGetDiagnosesByPatientId() throws Exception {
        String patientId = "patient1";
        String staffId = "doctor1";

        Diagnose diagnose1 = new Diagnose();
        diagnose1.setId(1L);
        diagnose1.setPatientUserId(patientId);
        diagnose1.setStaffUserId(staffId);
        diagnose1.setDiagnose("mock diagnose 2");
        diagnose1.setDetails("mock details 1");
        diagnose1.setDiagnosisDate(LocalDate.of(2023, 1, 15));

        Diagnose diagnose2 = new Diagnose();
        diagnose2.setId(2L);
        diagnose2.setPatientUserId(patientId);
        diagnose2.setStaffUserId(staffId);
        diagnose2.setDiagnose("mock diagnose 2");
        diagnose2.setDetails("mock details 2"); 
        diagnose2.setDiagnosisDate(LocalDate.of(2023, 2, 20));

        List<Diagnose> diagnoses = Arrays.asList(diagnose1, diagnose2);

        when(diagnoseService.findDiagnosesByPatientUserId(patientId)).thenReturn(diagnoses);

        JwtAuthenticationToken authentication = createMockAuthenticationToken(staffId, "Doctor");

        mockMvc.perform(get("/diagnoses/patient/{id}", patientId)
                        .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].patientUserId").value(patientId))
                .andExpect(jsonPath("$[0].staffUserId").value(staffId))
                .andExpect(jsonPath("$[0].diagnose").value("mock diagnose 1"))
                .andExpect(jsonPath("$[0].details").value("mock details 1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].patientUserId").value(patientId))
                .andExpect(jsonPath("$[1].staffUserId").value(staffId))
                .andExpect(jsonPath("$[1].diagnose").value("mock diagnose 2"))
                .andExpect(jsonPath("$[1].details").value("mock details 2"));
    }

    @Test
    public void testGetDiagnosesByPatientIdNotFound() throws Exception {
        String patientId = "nonexistent";

        when(diagnoseService.findDiagnosesByPatientUserId(patientId)).thenReturn(Collections.emptyList());

        JwtAuthenticationToken authentication = createMockAuthenticationToken("doctor1", "Doctor");

        mockMvc.perform(get("/diagnoses/patient/{id}", patientId)
                        .with(authentication(authentication)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetDiagnosesByPatientIdWithoutAuthentication() throws Exception {
        String patientId = "patient1";

        mockMvc.perform(get("/diagnoses/patient/{id}", patientId))
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
