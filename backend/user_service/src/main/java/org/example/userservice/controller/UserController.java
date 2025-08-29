package org.example.userservice.controller;

import org.example.userservice.dto.*;
import org.example.userservice.model.User;
import org.example.userservice.service.UserService;
import org.jboss.resteasy.client.jaxrs.internal.ClientResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.core.Response;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/user")
public class UserController {

    @Value("${keycloak.credentials.secret}")
    private String keycloakSecret;

    private final WebClient.Builder webClientBuilder;
    private final UserService userService;

    @Autowired
    public UserController(WebClient.Builder webClientBuilder, UserService userService) {
        this.userService = userService;
        this.webClientBuilder = webClientBuilder;
    }

    @GetMapping("/test")
    public String test(Authentication authentication) {
        System.out.println("User service is up and running!");

        // Retrieve JWT from Authentication object
        if (authentication != null && authentication.getCredentials() instanceof Jwt) {
            Jwt jwt = (Jwt) authentication.getCredentials();
            System.out.println("JWT Token: " + jwt.getTokenValue());
        } else {
            System.out.println("No JWT token found in the authentication context.");
        }

        return "User service is up and running!";
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(@RequestBody String payload) {
        System.out.println("Webhook called with payload: " + payload);

        ObjectMapper mapper = new ObjectMapper();
        try {
            User user = mapper.readValue(payload, User.class);
            System.out.println("User: " + user.toString());

            userService.saveUser(user);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    // @PostMapping("/register")
    // public ResponseEntity<LoginResponseDTO> registerUser(@RequestBody
    // RegisterRequestDTO registerRequestDTO) {
    // System.out.println("registerUser() called");

    // User user = userService.registerUser(registerRequestDTO);
    // if (user == null) {
    // return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
    // }

    // JwtDTO jwtDTO = new JwtDTO(user.getUsername(), user.getRole().toString(),
    // user.getId());
    // // String token = GenerateJWTToken(jwtDTO);
    // String token = GenerateKeycloakToken(registerRequestDTO.getUsername(),
    // registerRequestDTO.getPassword());

    // return ResponseEntity.status(HttpStatus.CREATED).body(new
    // LoginResponseDTO(token));
    // }

    // @PostMapping("/login")
    // public ResponseEntity<LoginResponseDTO> loginUser(@RequestBody
    // LoginRequestDTO loginRequestDTO) {
    // System.out.println("loginUser() called");

    // User user = userService.authenticateUser(loginRequestDTO.getUsername(),
    // loginRequestDTO.getPassword());
    // if (user == null) {
    // return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
    // }
    // System.out.println("Username = " + user.getUsername());

    // // Generate token
    // String token = GenerateKeycloakToken(loginRequestDTO.getUsername(),
    // loginRequestDTO.getPassword());

    // return ResponseEntity.ok(new LoginResponseDTO(token));
    // }

    @GetMapping("/profile")
    @PreAuthorize("hasAuthority('Patient') or hasAuthority('Doctor') or hasAuthority('Other_Staff')")
    public ResponseEntity<UserProfileDTO> getUserProfile(Authentication authentication) {
        System.out.println("getUserProfile() called");

        String userId = authentication.getName();

        if (userId == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        User user = userService.findByUserId(userId);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        UserProfileDTO profile = new UserProfileDTO(userId, user.getUsername(), user.getRole(), user.getFirstName(),
                user.getLastName());

        System.out.println(profile.toString());
        return new ResponseEntity<>(profile, HttpStatus.OK);
    }

    // Get all users
    @GetMapping
    @PreAuthorize("hasAuthority('Doctor') or hasAuthority('Other_Staff')")
    public ResponseEntity<List<User>> getAllUsers() {
        System.out.println("getAllUsers() called");
        return new ResponseEntity<>(userService.findAllUsers(), HttpStatus.OK);
    }

    // Get all patients
    @GetMapping("/patients")
    @PreAuthorize("hasAuthority('Doctor') or hasAuthority('Other_Staff')")
    public ResponseEntity<List<User>> getAllPatients() {
        System.out.println("getAllPatients() called");
        List<User> patients = userService.findAllPatients();
        for (User patient : patients) {
            System.out.println(patient.toString());
        }
        return new ResponseEntity<>(patients, HttpStatus.OK);
    }

    // // Update user
    // @PutMapping("/profile/{id}")
    // @PreAuthorize("hasAuthority('Patient') or hasAuthority('Doctor') or
    // hasAuthority('Other_Staff')")
    // public ResponseEntity<User> updateUser(
    // @RequestHeader("Authorization") String token,
    // @RequestBody UserProfileDTO userProfileDTO) {
    // System.out.println("updateUser() called");

    // User user = userService.findById(userProfileDTO.getId())
    // .orElseThrow(() -> new IllegalArgumentException("User not found"));
    // user.setUsername(userProfileDTO.getUsername());
    // user.setEmail(userProfileDTO.getEmail());
    // userService.updateUser(user);

    // if (Role.valueOf(userProfileDTO.getRole()) == Role.Patient) {
    // Patient patient = patientService.findPatientByUserId(user.getId());
    // patient.setFirstName(userProfileDTO.getFirstName());
    // patient.setLastName(userProfileDTO.getLastName());
    // patient.setSSN(userProfileDTO.getSSN());
    // patient.setEmail(userProfileDTO.getEmail());
    // patientService.updatePatient(patient);
    // } else if (Role.valueOf(userProfileDTO.getRole()) == Role.Doctor
    // || Role.valueOf(userProfileDTO.getRole()) == Role.Other_Staff) {
    // Optional<Staff> optionalStaff = staffService.findStaffByUserId(user.getId());
    // if (optionalStaff.isPresent()) {
    // Staff staff = optionalStaff.get();
    // staff.setFirstName(userProfileDTO.getFirstName());
    // staff.setLastName(userProfileDTO.getLastName());
    // staff.setSSN(userProfileDTO.getSSN());
    // staff.setEmail(userProfileDTO.getEmail());
    // staffService.updateStaff(staff);
    // } else {
    // return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    // }
    // } else {
    // return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
    // }
    // System.out.println("everything is fine :()");
    // return new ResponseEntity<>(user, HttpStatus.OK);
    // }

    // Get user details by ID
    @GetMapping("/details/{userId}")
    @PreAuthorize("hasAuthority('Doctor') or hasAuthority('Patient')")
    public ResponseEntity<PatientProfileDetailsDTO> getUserDetailsById(Authentication authentication,
            @PathVariable String userId) {
        System.out.println("getUserById() called with userId: " + userId);

        String requesterId = authentication.getName();

        System.out.println("authority of user: " + authentication.getAuthorities());

        if (requesterId == null) {
            System.out.println("requestedId is null");
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } else if (!(authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equalsIgnoreCase("Doctor") ||
                        auth.getAuthority().equalsIgnoreCase("ROLE_Doctor")) ||
                requesterId.equals(userId))) {
            System.out.println("User is not authorized to view this profile");
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        System.out.println("User is authorized to view this profile");

        String token = null;
        if (authentication != null && authentication.getCredentials() instanceof Jwt) {
            Jwt jwt = (Jwt) authentication.getCredentials();
            token = jwt.getTokenValue();
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        List<DiagnoseDTO> diagnoseList = getDiagnosesByUserId(userId, token);
        List<EncounterDTO> encounterList = getEncountersByUserId(userId, token);
        List<ObservationDTO> observationList = getObservationsByUserId(userId, token);

        User user = userService.findByUserId(userId);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        PatientProfileDetailsDTO userDetails = new PatientProfileDetailsDTO(userId, user.getFirstName(),
                user.getLastName(), diagnoseList, encounterList, observationList);
        return new ResponseEntity<>(userDetails, HttpStatus.OK);
    }

    // Get user by username
    @GetMapping("/username/{username}")
    public ResponseEntity<String> getUserByUsername(@PathVariable String username) {
        System.out.println("getUserByUsername() called with username: " + username);

        User user = userService.findByUsername(username);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        System.out.println("User found with userId: " + user.getUserId());

        return new ResponseEntity<>(user.getUserId(), HttpStatus.OK);
    }

    // Get user by userId
    @GetMapping("/{userId}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable String userId) {
        System.out.println("getUserById() called with userId: " + userId);

        User user = userService.findByUserId(userId);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        UserDTO userDTO = new UserDTO(user.getUserId(), user.getUsername());
        return new ResponseEntity<>(userDTO, HttpStatus.OK);
    }

    // // Delete user
    // @DeleteMapping("/{id}")
    // public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
    // System.out.println("deleteUser() called");
    // User user = userService.findById(id)
    // .orElseThrow(() -> new IllegalArgumentException("User not found"));
    // if (user.getRole() == Role.Patient) {
    // patientService.deletePatient(id);
    // } else if (user.getRole() == Role.Doctor || user.getRole() ==
    // Role.Other_Staff) {
    // staffService.deleteStaff(id);
    // }
    // userService.deleteUser(id);
    // return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    // }

    public List<DiagnoseDTO> getDiagnosesByUserId(String patientUserId, String token) {
        String medicalDataServiceURL = "http://medical-data-service:8084/diagnoses/patient/" + patientUserId;
        return this.webClientBuilder.build()
                .get()
                .uri(medicalDataServiceURL)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .onStatus(status -> status == HttpStatus.NOT_FOUND, clientResponse -> Mono.empty())
                .bodyToFlux(DiagnoseDTO.class)
                .collectList()
                .block();
    }

    public List<EncounterDTO> getEncountersByUserId(String patientUserId, String token) {
        String medicalDataServiceURL = "http://medical-data-service:8084/encounters/patient/" + patientUserId;
        return this.webClientBuilder.build()
                .get()
                .uri(medicalDataServiceURL)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .onStatus(status -> status == HttpStatus.NOT_FOUND, clientResponse -> Mono.empty())
                .bodyToFlux(EncounterDTO.class)
                .collectList()
                .block();
    }

    public List<ObservationDTO> getObservationsByUserId(String patientUserId, String token) {
        String medicalDataServiceURL = "http://medical-data-service:8084/observations/patient/" + patientUserId;
        return this.webClientBuilder.build()
                .get()
                .uri(medicalDataServiceURL)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .onStatus(status -> status == HttpStatus.NOT_FOUND, clientResponse -> Mono.empty())
                .bodyToFlux(ObservationDTO.class)
                .collectList()
                .block();
    }

}