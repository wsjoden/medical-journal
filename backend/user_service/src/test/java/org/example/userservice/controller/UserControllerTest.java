package org.example.userservice.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.userservice.dto.*;
import org.example.userservice.model.User;
import org.example.userservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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

    @SpyBean
    private UserController userController;

    @MockBean
    private WebClient.Builder webClientBuilder;

    @BeforeEach
    public void setup() {
        doReturn(Collections.emptyList()).when(userController).getDiagnosesByUserId(anyString(), anyString());
        doReturn(Collections.emptyList()).when(userController).getEncountersByUserId(anyString(), anyString());
        doReturn(Collections.emptyList()).when(userController).getObservationsByUserId(anyString(), anyString());
    }

    @Test
    public void testEndpointWithAuthentication() throws Exception {
        Jwt jwt = createMockJwt();
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")), "user123");

        mockMvc.perform(get("/user/test").with(authentication(authentication))).andExpect(status().isOk()).andExpect(content().string("User service is up and running!"));
    }

    @Test
    public void testEndpointWithoutAuthentication() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/user/test")).andExpect(status().isUnauthorized());
    }

    @Test
    public void testWebhookWithValidPayload() throws Exception {
        User testUser = new User();
        testUser.setUserId("123");
        testUser.setUsername("Test User");

        String userJson = objectMapper.writeValueAsString(testUser);

        mockMvc.perform(post("/user/webhook").contentType(MediaType.APPLICATION_JSON).content(userJson)).andExpect(status().isOk());
        verify(userService, times(1)).saveUser(any(User.class));
    }

    @Test
    public void testWebhookWithInvalidPayload() throws Exception {
        String invalidJson = "{\"id\":\"123\", \"name\": unclosed string}";

        mockMvc.perform(post("/user/webhook").contentType(MediaType.APPLICATION_JSON).content(invalidJson)).andExpect(status().isOk()); // Note: Your endpoint returns 200 OK even for invalid payloads
        verify(userService, times(0)).saveUser(any(User.class));
    }

    @Test
    public void testWebhookWithMissingFields() throws Exception {
        String incompleteJson = "{\"id\":\"123\"}"; // Missing other required fields
        mockMvc.perform(post("/user/webhook").contentType(MediaType.APPLICATION_JSON).content(incompleteJson)).andExpect(status().isOk());
    }

    @Test
    public void testGetProfileAsPatient() throws Exception {
        User user = new User();
        user.setUserId("123");
        user.setUsername("patientUser");
        user.setRole("Patient");
        user.setFirstName("John");
        user.setLastName("Doe");

        when(userService.findByUserId("123")).thenReturn(user);

        JwtAuthenticationToken authentication = createMockAuthenticationToken("123", "Patient");

        mockMvc.perform(get("/user/profile").with(authentication(authentication))).andExpect(status().isOk()).andExpect(jsonPath("$.userId").value("123")).andExpect(jsonPath("$.username").value("patientUser")).andExpect(jsonPath("$.role").value("Patient")).andExpect(jsonPath("$.firstName").value("John")).andExpect(jsonPath("$.lastName").value("Doe"));
    }

    @Test
    public void testGetProfileAsDoctor() throws Exception {
        User user = new User();
        user.setUserId("456");
        user.setUsername("doctorUser");
        user.setRole("Doctor");
        user.setFirstName("Jane");
        user.setLastName("Smith");

        when(userService.findByUserId("456")).thenReturn(user);

        JwtAuthenticationToken authentication = createMockAuthenticationToken("456", "Doctor");

        mockMvc.perform(get("/user/profile").with(authentication(authentication))).andExpect(status().isOk()).andExpect(jsonPath("$.userId").value("456")).andExpect(jsonPath("$.username").value("doctorUser")).andExpect(jsonPath("$.role").value("Doctor")).andExpect(jsonPath("$.firstName").value("Jane")).andExpect(jsonPath("$.lastName").value("Smith"));
    }

    @Test
    public void testGetProfileUnauthorized() throws Exception {
        when(userService.findByUserId("789")).thenReturn(null);

        JwtAuthenticationToken authentication = createMockAuthenticationToken("789", "admin");
        mockMvc.perform(get("/user/profile").with(authentication(authentication))).andExpect(status().isForbidden());
    }

    @Test
    public void testGetProfileNotFound() throws Exception {
        when(userService.findByUserId("unknown")).thenReturn(null);

        JwtAuthenticationToken authentication = createMockAuthenticationToken("unknown", "Patient");
        mockMvc.perform(get("/user/profile").with(authentication(authentication))).andExpect(status().isNotFound());
    }

    @Test
    public void testGetProfileWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/user/profile")).andExpect(status().isUnauthorized());
    }

    @Test
    public void testGetAllPatientsAsDoctor() throws Exception {
        User patient1 = new User("patient1", "Patient", "John", "Doe", "Patient");
        User patient2 = new User("patient2", "Patient", "Jane", "Smith", "Patient");

        List<User> patients = Arrays.asList(patient1, patient2);

        when(userService.findAllPatients()).thenReturn(patients);

        JwtAuthenticationToken authentication = createMockAuthenticationToken("doctor1", "Doctor");

        MvcResult result = mockMvc.perform(get("/user/patients").with(authentication(authentication))).andExpect(status().isOk()).andReturn();

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

        JwtAuthenticationToken authentication = createMockAuthenticationToken("staff1", "Other_Staff");

        mockMvc.perform(get("/user/patients").with(authentication(authentication))).andExpect(status().isOk()).andExpect(jsonPath("$[0].role").value("Patient")).andExpect(jsonPath("$[1].role").value("Patient"));
    }

    @Test
    public void testGetAllPatientsAsPatient() throws Exception {
        JwtAuthenticationToken authentication = createMockAuthenticationToken("patient1", "Patient");
        mockMvc.perform(get("/user/patients").with(authentication(authentication))).andExpect(status().isForbidden());
    }

    @Test
    public void testGetAllUsersWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/user")).andExpect(status().isUnauthorized());
    }

    @Test
    public void testGetAllPatientsWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/user/patients")).andExpect(status().isUnauthorized());
    }

    @Test
    public void testGetAllUsersWithEmptyResult() throws Exception {
        when(userService.findAllUsers()).thenReturn(Collections.emptyList());

        JwtAuthenticationToken authentication = createMockAuthenticationToken("doctor1", "Doctor");
        mockMvc.perform(get("/user").with(authentication(authentication))).andExpect(status().isOk()).andExpect(jsonPath("$").isArray()).andExpect(jsonPath("$").isEmpty());
    }

    @Test
    public void testPatientDetailsByUserIdAsSamePatient() throws Exception {
        String userId = "patient1";
        User patient = new User(userId, "Patient", "John", "Doe", "Patient");

        when(userService.findByUserId(userId)).thenReturn(patient);

        List<DiagnoseDTO> diagnoses = Collections.singletonList(new DiagnoseDTO(1L, userId, "doctor1", "Diabetes", "Type 2", LocalDate.of(2023, 1, 15)));
        List<EncounterDTO> encounters = Collections.singletonList(new EncounterDTO(1L, userId, "doctor1", "Regular checkup", LocalDate.of(2023, 1, 15)));
        List<ObservationDTO> observations = Collections.singletonList(new ObservationDTO(1L, userId, "doctor1", "Blood pressure reading", LocalDate.of(2023, 1, 15)));

        doReturn(diagnoses).when(userController).getDiagnosesByUserId(eq(userId), anyString());
        doReturn(encounters).when(userController).getEncountersByUserId(eq(userId), anyString());
        doReturn(observations).when(userController).getObservationsByUserId(eq(userId), anyString());

        JwtAuthenticationToken authentication = createMockAuthenticationToken(userId, "Patient");

        mockMvc.perform(get("/user/details/{userId}", userId).with(authentication(authentication))).andExpect(status().isOk()).andExpect(jsonPath("$.userId").value(userId)).andExpect(jsonPath("$.firstName").value("John")).andExpect(jsonPath("$.lastName").value("Doe"));
    }

    @Test
    public void testGetUserDetailsByIdAsDoctor() throws Exception {
        String doctorId = "doctor1";
        String patientId = "patient1";
        User patient = new User(patientId, "Patient", "John", "Doe", "Patient");

        when(userService.findByUserId(patientId)).thenReturn(patient);

        List<DiagnoseDTO> diagnoses = Collections.singletonList(new DiagnoseDTO(1L, patientId, doctorId, "Hypertension", "Stage 1", LocalDate.of(2023, 2, 20)));
        List<EncounterDTO> encounters = Collections.singletonList(new EncounterDTO(2L, patientId, doctorId, "Blood pressure check", LocalDate.of(2023, 2, 20)));
        List<ObservationDTO> observations = Collections.singletonList(new ObservationDTO(2L, patientId, doctorId, "Blood pressure 140/90", LocalDate.of(2023, 2, 20)));

        doReturn(diagnoses).when(userController).getDiagnosesByUserId(eq(patientId), anyString());
        doReturn(encounters).when(userController).getEncountersByUserId(eq(patientId), anyString());
        doReturn(observations).when(userController).getObservationsByUserId(eq(patientId), anyString());

        JwtAuthenticationToken authentication = createMockAuthenticationToken(doctorId, "Doctor");

        mockMvc.perform(get("/user/details/{userId}", patientId).with(authentication(authentication))).andExpect(status().isOk()).andExpect(jsonPath("$.userId").value(patientId)).andExpect(jsonPath("$.firstName").value("John")).andExpect(jsonPath("$.lastName").value("Doe"));
    }

    @Test
    public void testGetUserDetailsByIdAsDifferentPatient() throws Exception {
        String requesterId = "patient2";
        String targetId = "patient1";

        User patient = new User(targetId, "Patient", "John", "Doe", "Patient");
        when(userService.findByUserId(targetId)).thenReturn(patient);

        JwtAuthenticationToken authentication = createMockAuthenticationToken(requesterId, "Patient");

        mockMvc.perform(get("/user/details/{userId}", targetId).with(authentication(authentication))).andExpect(status().isForbidden());
    }

    @Test
    public void testGetUserDetailsByIdWithInvalidUser() throws Exception {
        String doctorId = "doctor1";
        String nonExistentId = "nonexistent";

        when(userService.findByUserId(nonExistentId)).thenReturn(null);

        JwtAuthenticationToken authentication = createMockAuthenticationToken(doctorId, "Doctor");

        mockMvc.perform(get("/user/details/{userId}", nonExistentId).with(authentication(authentication))).andExpect(status().isNotFound());
    }

    @Test
    public void testGetUserDetailsByIdWithoutAuthentication() throws Exception {
        String patientId = "patient1";
        mockMvc.perform(get("/user/details/{userId}", patientId)).andExpect(status().isUnauthorized());
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

        JwtAuthenticationToken authentication = createMockAuthenticationToken("admin1", "Admin");

        mockMvc.perform(get("/user/username/{username}", username).with(authentication(authentication))).andExpect(status().isOk()).andExpect(content().string(userId));
    }

    @Test
    public void testGetUserByUsernameNotFound() throws Exception {
        String nonExistentUsername = "nonexistentuser";
        when(userService.findByUsername(nonExistentUsername)).thenReturn(null);

        JwtAuthenticationToken authentication = createMockAuthenticationToken("admin1", "Admin");

        mockMvc.perform(get("/user/username/{username}", nonExistentUsername).with(authentication(authentication))).andExpect(status().isNotFound());
    }

    @Test
    public void testGetUserByUserIdFound() throws Exception {
        String userId = "user123";
        String username = "testuser";

        User mockUser = new User();
        mockUser.setUserId(userId);
        mockUser.setUsername(username);
        mockUser.setFirstName("Test");
        mockUser.setLastName("User");

        when(userService.findByUserId(userId)).thenReturn(mockUser);

        JwtAuthenticationToken authentication = createMockAuthenticationToken("admin1", "Admin");

        mockMvc.perform(get("/user/{userId}", userId).with(authentication(authentication))).andExpect(status().isOk()).andExpect(jsonPath("$.userId").value(userId)).andExpect(jsonPath("$.username").value(username));
    }

    @Test
    public void testGetUserByUserIdNotFound() throws Exception {
        String nonExistentUserId = "nonexistentid";
        when(userService.findByUserId(nonExistentUserId)).thenReturn(null);

        JwtAuthenticationToken authentication = createMockAuthenticationToken("admin1", "Admin");
        mockMvc.perform(get("/user/{userId}", nonExistentUserId).with(authentication(authentication))).andExpect(status().isNotFound());
    }

    @Test
    public void testGetUserByUserIdWithSpecialCharacters() throws Exception {
        String specialUserId = "user-123+456@domain";
        String username = "specialuser";

        User mockUser = new User();
        mockUser.setUserId(specialUserId);
        mockUser.setUsername(username);

        when(userService.findByUserId(specialUserId)).thenReturn(mockUser);

        JwtAuthenticationToken authentication = createMockAuthenticationToken("admin1", "Admin");
        mockMvc.perform(get("/user/{userId}", specialUserId).with(authentication(authentication))).andExpect(status().isOk()).andExpect(jsonPath("$.userId").value(specialUserId));
    }

    @Test
    public void testGetUserByUsernameWithLongUsername() throws Exception {
        String longUsername = "thisissuperlongusernamethatcouldpotentiallycauseproblemsinitslength";
        String userId = "user789";

        User mockUser = new User();
        mockUser.setUserId(userId);
        mockUser.setUsername(longUsername);

        when(userService.findByUsername(longUsername)).thenReturn(mockUser);

        JwtAuthenticationToken authentication = createMockAuthenticationToken("admin1", "Admin");
        mockMvc.perform(get("/user/username/{username}", longUsername).with(authentication(authentication))).andExpect(status().isOk()).andExpect(content().string(userId));
    }

    @Test
    public void testGetUserByUserIdReturnsCorrectDTO() throws Exception {
        String userId = "user456";
        String username = "regularuser";

        User mockUser = new User();
        mockUser.setUserId(userId);
        mockUser.setUsername(username);
        mockUser.setFirstName("Regular");
        mockUser.setLastName("User");
        mockUser.setRole("Patient");

        when(userService.findByUserId(userId)).thenReturn(mockUser);

        JwtAuthenticationToken authentication = createMockAuthenticationToken("admin1", "Admin");
        MvcResult result = mockMvc.perform(get("/user/{userId}", userId).with(authentication(authentication))).andExpect(status().isOk()).andReturn();

        String responseJson = result.getResponse().getContentAsString();
        UserDTO returnedDTO = objectMapper.readValue(responseJson, UserDTO.class);

        assertEquals(userId, returnedDTO.getUserId());
        assertEquals(username, returnedDTO.getUsername());
    }

    @Test
    public void testGetUserByUserIdWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/user/{userId}", "someUserId")).andExpect(status().isUnauthorized());
    }

    @Test
    public void testGetUserByUsernameWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/user/username/{username}", "someUsername")).andExpect(status().isUnauthorized());
    }

    @Test
    public void debugAuthorities() throws Exception {
        JwtAuthenticationToken authentication = createMockAuthenticationToken("patient1", "Patient");

        MvcResult result = mockMvc.perform(get("/user/patients").with(authentication(authentication))).andReturn();

        System.out.println("Status: " + result.getResponse().getStatus());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            System.out.println("Authentication class: " + auth.getClass().getName());
            System.out.println("Principal: " + auth.getPrincipal());
            System.out.println("Authorities: " + auth.getAuthorities());

            for (GrantedAuthority authority : auth.getAuthorities()) {
                System.out.println("Authority: '" + authority.getAuthority() + "'");
            }
        } else {
            System.out.println("No authentication in context");
        }
    }

    private Jwt createMockJwt() {
        Map<String, Object> headers = new HashMap<>();
        headers.put("alg", "RS256");

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "user123");
        claims.put("iss", "localhost:8080/");
        claims.put("role", "USER");

        return new Jwt("token-value-for-testing", Instant.now(), Instant.now().plusSeconds(3600), headers, claims);
    }

    private JwtAuthenticationToken createMockAuthenticationToken(String userId, String role) {
        Map<String, Object> headers = new HashMap<>();
        headers.put("alg", "RS256");

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", userId);
        claims.put("iss", "localhost:8080/");
        claims.put("role", role);

        Jwt jwt = new Jwt("token-value-for-testing", Instant.now(), Instant.now().plusSeconds(3600), headers, claims);

        return new JwtAuthenticationToken(jwt, Collections.singletonList(new SimpleGrantedAuthority(role)), userId);
    }
}