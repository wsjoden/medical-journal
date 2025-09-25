package org.example.userservice.service;

import org.example.userservice.dto.*;
import org.example.userservice.model.User;
import org.example.userservice.repository.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Value("${medical.data.service.url}")
    private String medicalDataServiceUrl;

    private final IUserRepository userRepository;
    private final WebClient.Builder webClientBuilder;

    @Autowired
    public UserService(IUserRepository userRepository, WebClient.Builder webClientBuilder) {
        this.userRepository = userRepository;
        this.webClientBuilder = webClientBuilder;
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User findByUserId(String userId) {
        return userRepository.findByUserId(userId);
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public List<User> findAllPatients() {
        return userRepository.findAllPatients();
    }

    public UserProfileDTO getUserProfileDTOByUserId(String userId, Authentication authentication) {
        User user = findByUserId(userId);
        if (user == null) {
            user = createUserFromJwt(authentication);
            if (user != null) {
                saveUser(user);
            } else {
                return null;
            }
        }
        return new UserProfileDTO(
                userId,
                user.getUsername(),
                user.getRole(),
                user.getFirstName(),
                user.getLastName());
    }

    public UserDTO getUserDTOByUserId(String userId, Authentication authentication) {
        User user = findByUserId(userId);

        if (user == null) {
            user = createUserFromJwt(authentication);
            if (user != null) {
                saveUser(user);
            } else {
                return null;
            }
        }
        return new UserDTO(
                userId,
                user.getUsername());
    }

    // Check if user is Doctor or requester == userId
    public Boolean userDetailsAuthentication(String requesterId, String userId, Authentication authentication) {
        boolean isDoctor = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equalsIgnoreCase("Doctor")
                        || auth.getAuthority().equalsIgnoreCase("ROLE_Doctor"));

        return isDoctor || requesterId.equals(userId);
    }

    public PatientProfileDetailsDTO getPatientDetails(String userId, String token, Authentication authentication) {

        User user = findByUserId(userId);
        // If user is not in db, add it.
        if (user == null) {
            user = createUserFromJwt(authentication);
            if (user != null) {
                saveUser(user);
            } else {
                return null;
            }
        }

        // Fetch medical data
        List<DiagnoseDTO> diagnoses = getDiagnosesByUserId(userId, token);
        List<EncounterDTO> encounters = getEncountersByUserId(userId, token);
        List<ObservationDTO> observations = getObservationsByUserId(userId, token);

        return new PatientProfileDetailsDTO(
                userId,
                user.getFirstName(),
                user.getLastName(),
                diagnoses,
                encounters,
                observations);
    }

    // Create user from JWT claims
    private User createUserFromJwt(Authentication authentication) {
        if (authentication.getCredentials() instanceof Jwt) {
            Jwt jwt = (Jwt) authentication.getCredentials();

            User user = new User();
            user.setUserId(jwt.getSubject());
            user.setUsername(jwt.getClaimAsString("preferred_username"));
            user.setFirstName(jwt.getClaimAsString("given_name"));
            user.setLastName(jwt.getClaimAsString("family_name"));
            user.setRole(jwt.getClaimAsString("role"));

            return user;
        }
        return null;
    }

    // Fetch Diagnoses from Medical Data Service
    private List<DiagnoseDTO> getDiagnosesByUserId(String patientUserId, String token) {
        String url = medicalDataServiceUrl + "/diagnoses/patient/" + patientUserId;
        return this.webClientBuilder.build()
                .get()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .onStatus(status -> status == HttpStatus.NOT_FOUND, clientResponse -> Mono.empty())
                .bodyToFlux(DiagnoseDTO.class)
                .collectList()
                .block();
    }

    // Fetch Encounters from Medical Data Service
    private List<EncounterDTO> getEncountersByUserId(String patientUserId, String token) {
        String url = medicalDataServiceUrl + "/encounters/patient/" + patientUserId;
        return this.webClientBuilder.build()
                .get()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .onStatus(status -> status == HttpStatus.NOT_FOUND, clientResponse -> Mono.empty())
                .bodyToFlux(EncounterDTO.class)
                .collectList()
                .block();
    }

    // Fetch Observations from Medical Data Service
    private List<ObservationDTO> getObservationsByUserId(String patientUserId, String token) {
        String url = medicalDataServiceUrl + "/observations/patient/" + patientUserId;
        return this.webClientBuilder.build()
                .get()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .onStatus(status -> status == HttpStatus.NOT_FOUND, clientResponse -> Mono.empty())
                .bodyToFlux(ObservationDTO.class)
                .collectList()
                .block();
    }
}
