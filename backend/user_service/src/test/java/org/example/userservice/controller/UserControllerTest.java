package org.example.userservice.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.userservice.dto.*;
import org.example.userservice.model.User;
import org.example.userservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private WebClient.Builder webClientBuilder;

    @BeforeEach
    public void setup() {
        // Reset mocks before each test
        reset(userService);
    }

    @Test
    public void testEndpointWithAuthentication() throws Exception {
        Jwt jwt = createMockJwt();
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")), "user123");

        mockMvc.perform(get("/user/test").with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(content().string("User service is up and running!"));
    }

    @Test
    public void testEndpointWithoutAuthentication() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/user/test"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testGetProfileAsPatient() throws Exception {
        String userId = "123";
        UserProfileDTO profileDTO = new UserProfileDTO(userId, "patientUser", "Patient", "John", "Doe");

        when(userService.getUserProfile(eq(userId), any())).thenReturn(profileDTO);

        JwtAuthenticationToken authentication = createMockAuthenticationToken(userId, "ROLE_Patient");

        mockMvc.perform(get("/user/profile").with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.username").value("patientUser"))
                .andExpect(jsonPath("$.role").value("Patient"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));
    }

    @Test
    public void testGetProfileAsDoctor() throws Exception {
        String userId = "456";
        UserProfileDTO profileDTO = new UserProfileDTO(userId, "doctorUser", "Doctor", "Jane", "Smith");

        when(userService.getUserProfile(eq(userId), any())).thenReturn(profileDTO);

        JwtAuthenticationToken authentication = createMockAuthenticationToken(userId, "ROLE_Doctor");

        mockMvc.perform(get("/user/profile").with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.username").value("doctorUser"))
                .andExpect(jsonPath("$.role").value("Doctor"))
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Smith"));
    }

    @Test
    public void testGetProfileUnauthorized() throws Exception {
        JwtAuthenticationToken authentication = createMockAuthenticationToken("789", "ROLE_Admin");
        mockMvc.perform(get("/user/profile").with(authentication(authentication)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testGetProfileInternalError() throws Exception {
        String userId = "123";
        when(userService.getUserProfile(eq(userId), any())).thenReturn(null);

        JwtAuthenticationToken authentication = createMockAuthenticationToken(userId, "ROLE_Patient");
        mockMvc.perform(get("/user/profile").with(authentication(authentication)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void testGetProfileWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/user/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testGetAllPatientsAsDoctor() throws Exception {
        User patient1 = new User("patient1", "Patient", "John", "Doe", "Patient");
        User patient2 = new User("patient2", "Patient", "Jane", "Smith", "Patient");

        List<User> patients = Arrays.asList(patient1, patient2);

        when(userService.findAllPatients()).thenReturn(patients);

        JwtAuthenticationToken authentication = createMockAuthenticationToken("doctor1", "ROLE_Doctor");

        MvcResult result = mockMvc.perform(get("/user/patients").with(authentication(authentication)))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        List<User> returnedPatients = objectMapper.readValue(responseJson, new TypeReference<List<User>>() {
        });

        assertEquals(2, returnedPatients.size());
        assertEquals("Patient", returnedPatients.get(0).getRole());
        assertEquals("Patient", returnedPatients.get(1).getRole());
        assertTrue(returnedPatients.stream().anyMatch(user -> user.getUserId().equals("patient1")));
        assertTrue(returnedPatients.stream().anyMatch(user -> user.getUserId().equals("patient2")));
    }

    @Test
    public void testGetAllPatientsAsStaff() throws Exception {
        User patient1 = new User("patient1", "Patient", "John", "Doe", "Patient");
        User patient2 = new User("patient2", "Patient", "Jane", "Smith", "Patient");

        List<User> patients = Arrays.asList(patient1, patient2);

        when(userService.findAllPatients()).thenReturn(patients);

        JwtAuthenticationToken authentication = createMockAuthenticationToken("staff1", "ROLE_Other_Staff");

        mockMvc.perform(get("/user/patients").with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].role").value("Patient"))
                .andExpect(jsonPath("$[1].role").value("Patient"));
    }

    @Test
    public void testGetAllPatientsAsPatient() throws Exception {
        JwtAuthenticationToken authentication = createMockAuthenticationToken("patient1", "ROLE_Patient");
        mockMvc.perform(get("/user/patients").with(authentication(authentication)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testGetAllUsersWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/user"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testGetAllPatientsWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/user/patients"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testGetAllUsersWithEmptyResult() throws Exception {
        when(userService.findAllUsers()).thenReturn(Collections.emptyList());

        JwtAuthenticationToken authentication = createMockAuthenticationToken("doctor1", "ROLE_Doctor");
        mockMvc.perform(get("/user").with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    public void testPatientDetailsByUserIdAsSamePatient() throws Exception {
        String userId = "patient1";

        List<DiagnoseDTO> diagnoses = Collections.singletonList(
                new DiagnoseDTO(1L, userId, "doctor1", "Diabetes", "Type 2", LocalDate.of(2023, 1, 15)));
        List<EncounterDTO> encounters = Collections.singletonList(
                new EncounterDTO(1L, userId, "doctor1", "Regular checkup", LocalDate.of(2023, 1, 15)));
        List<ObservationDTO> observations = Collections.singletonList(
                new ObservationDTO(1L, userId, "doctor1", "Blood pressure reading", LocalDate.of(2023, 1, 15)));

        PatientProfileDetailsDTO detailsDTO = new PatientProfileDetailsDTO(
                userId, "John", "Doe", diagnoses, encounters, observations);

        when(userService.userDetailsAuthentication(eq(userId), eq(userId), any())).thenReturn(true);
        when(userService.getPatientDetails(eq(userId), anyString(), any())).thenReturn(detailsDTO);

        JwtAuthenticationToken authentication = createMockAuthenticationToken(userId, "ROLE_Patient");

        mockMvc.perform(get("/user/details/{userId}", userId).with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));
    }

    @Test
    public void testGetUserDetailsByIdAsDoctor() throws Exception {
        String doctorId = "doctor1";
        String patientId = "patient1";

        List<DiagnoseDTO> diagnoses = Collections.singletonList(
                new DiagnoseDTO(1L, patientId, doctorId, "Hypertension", "Stage 1", LocalDate.of(2023, 2, 20)));
        List<EncounterDTO> encounters = Collections.singletonList(
                new EncounterDTO(2L, patientId, doctorId, "Blood pressure check", LocalDate.of(2023, 2, 20)));
        List<ObservationDTO> observations = Collections.singletonList(
                new ObservationDTO(2L, patientId, doctorId, "Blood pressure 140/90", LocalDate.of(2023, 2, 20)));

        PatientProfileDetailsDTO detailsDTO = new PatientProfileDetailsDTO(
                patientId, "John", "Doe", diagnoses, encounters, observations);

        when(userService.userDetailsAuthentication(eq(doctorId), eq(patientId), any())).thenReturn(true);
        when(userService.getPatientDetails(eq(patientId), anyString(), any())).thenReturn(detailsDTO);

        JwtAuthenticationToken authentication = createMockAuthenticationToken(doctorId, "ROLE_Doctor");

        mockMvc.perform(get("/user/details/{userId}", patientId).with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(patientId))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));
    }

    @Test
    public void testGetUserDetailsByIdAsDifferentPatient() throws Exception {
        String requesterId = "patient2";
        String targetId = "patient1";

        when(userService.userDetailsAuthentication(eq(requesterId), eq(targetId), any())).thenReturn(false);

        JwtAuthenticationToken authentication = createMockAuthenticationToken(requesterId, "ROLE_Patient");

        mockMvc.perform(get("/user/details/{userId}", targetId).with(authentication(authentication)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testGetUserDetailsByIdWithInvalidUser() throws Exception {
        String doctorId = "doctor1";
        String nonExistentId = "nonexistent";

        when(userService.userDetailsAuthentication(eq(doctorId), eq(nonExistentId), any())).thenReturn(true);
        when(userService.getPatientDetails(eq(nonExistentId), anyString(), any())).thenReturn(null);

        JwtAuthenticationToken authentication = createMockAuthenticationToken(doctorId, "ROLE_Doctor");

        mockMvc.perform(get("/user/details/{userId}", nonExistentId).with(authentication(authentication)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetUserDetailsByIdWithoutAuthentication() throws Exception {
        String patientId = "patient1";
        mockMvc.perform(get("/user/details/{userId}", patientId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testGetUserByUsernameFound() throws Exception {
        String username = "testuser";
        String userId = "user123";

        User mockUser = new User();
        mockUser.setUserId(userId);
        mockUser.setUsername(username);
        mockUser.setFirstName("Test");
        mockUser.setLastName("User");

        when(userService.findByUsername(username)).thenReturn(mockUser);

        mockMvc.perform(get("/user/username/{username}", username))
                .andExpect(status().isOk())
                .andExpect(content().string(userId));
    }

    @Test
    public void testGetUserByUsernameNotFound() throws Exception {
        String nonExistentUsername = "nonexistentuser";
        when(userService.findByUsername(nonExistentUsername)).thenReturn(null);

        mockMvc.perform(get("/user/username/{username}", nonExistentUsername))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetUserByUserIdFound() throws Exception {
        String userId = "user123";
        String username = "testuser";

        UserDTO userDTO = new UserDTO(userId, username);
        when(userService.getUserDTOByUserId(eq(userId), any())).thenReturn(userDTO);

        JwtAuthenticationToken authentication = createMockAuthenticationToken("admin1", "ROLE_Patient");

        mockMvc.perform(get("/user/{userId}", userId).with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.username").value(username));
    }

    @Test
    public void testGetUserByUserIdNotFound() throws Exception {
        String nonExistentUserId = "nonexistentid";
        when(userService.getUserDTOByUserId(eq(nonExistentUserId), any())).thenReturn(null);

        JwtAuthenticationToken authentication = createMockAuthenticationToken("admin1", "ROLE_Patient");
        mockMvc.perform(get("/user/{userId}", nonExistentUserId).with(authentication(authentication)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetUserByUserIdWithSpecialCharacters() throws Exception {
        String specialUserId = "user-123+456@domain";
        String username = "specialuser";

        UserDTO userDTO = new UserDTO(specialUserId, username);
        when(userService.getUserDTOByUserId(eq(specialUserId), any())).thenReturn(userDTO);

        JwtAuthenticationToken authentication = createMockAuthenticationToken("admin1", "ROLE_Patient");
        mockMvc.perform(get("/user/{userId}", specialUserId).with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(specialUserId));
    }

    @Test
    public void testGetUserByUsernameWithLongUsername() throws Exception {
        String longUsername = "thisissuperlongusernamethatcouldpotentiallycauseproblemsinitslength";
        String userId = "user789";

        User mockUser = new User();
        mockUser.setUserId(userId);
        mockUser.setUsername(longUsername);

        when(userService.findByUsername(longUsername)).thenReturn(mockUser);

        mockMvc.perform(get("/user/username/{username}", longUsername))
                .andExpect(status().isOk())
                .andExpect(content().string(userId));
    }

    @Test
    public void testGetUserByUserIdReturnsCorrectDTO() throws Exception {
        String userId = "user456";
        String username = "regularuser";

        UserDTO userDTO = new UserDTO(userId, username);
        when(userService.getUserDTOByUserId(eq(userId), any())).thenReturn(userDTO);

        JwtAuthenticationToken authentication = createMockAuthenticationToken("admin1", "ROLE_Patient");
        MvcResult result = mockMvc.perform(get("/user/{userId}", userId).with(authentication(authentication)))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        UserDTO returnedDTO = objectMapper.readValue(responseJson, UserDTO.class);

        assertEquals(userId, returnedDTO.getUserId());
        assertEquals(username, returnedDTO.getUsername());
    }

    @Test
    public void testGetUserByUserIdWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/user/{userId}", "someUserId"))
                .andExpect(status().isUnauthorized());
    }

    private Jwt createMockJwt() {
        Map<String, Object> headers = new HashMap<>();
        headers.put("alg", "RS256");

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "user123");
        claims.put("iss", "localhost:8080/");
        claims.put("role", "USER");

        return new Jwt("token-value-for-testing", Instant.now(),
                Instant.now().plusSeconds(3600), headers, claims);
    }

    private JwtAuthenticationToken createMockAuthenticationToken(String userId, String role) {
        Map<String, Object> headers = new HashMap<>();
        headers.put("alg", "RS256");

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", userId);
        claims.put("iss", "localhost:8080/");
        claims.put("role", role);
        claims.put("preferred_username", "user_" + userId);
        claims.put("given_name", "Test");
        claims.put("family_name", "User");

        Jwt jwt = new Jwt("token-value-for-testing", Instant.now(),
                Instant.now().plusSeconds(3600), headers, claims);

        return new JwtAuthenticationToken(jwt,
                Collections.singletonList(new SimpleGrantedAuthority(role)), userId);
    }
}