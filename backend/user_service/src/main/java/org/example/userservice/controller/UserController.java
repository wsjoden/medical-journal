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

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
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

    @GetMapping("/profile")
    @PreAuthorize("hasRole('Patient') or hasRole('Doctor') or hasRole('Other_Staff')")
    public ResponseEntity<UserProfileDTO> getUserProfile(Authentication authentication) {
        String userId = authentication.getName();

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserProfileDTO profile = userService.getUserProfile(userId, authentication);

        if (profile == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        return ResponseEntity.ok(profile);
    }

    // Get all users
    @GetMapping
    @PreAuthorize("hasRole('Doctor') or hasRole('Other_Staff')")
    public ResponseEntity<List<User>> getAllUsers() {
        return new ResponseEntity<>(userService.findAllUsers(), HttpStatus.OK);
    }

    // Get all patients
    @GetMapping("/patients")
    @PreAuthorize("hasRole('Doctor') or hasRole('Other_Staff')")
    public ResponseEntity<List<User>> getAllPatients() {
        return ResponseEntity.ok(userService.findAllPatients());
    }

    // Get user details by ID
    @GetMapping("/details/{userId}")
    @PreAuthorize("hasRole('Doctor') or hasRole('Patient')")
    public ResponseEntity<PatientProfileDetailsDTO> getUserDetailsById(Authentication authentication,
            @PathVariable String userId) {

        String requesterId = authentication.getName();

        if (requesterId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Check if user is allowed to access requested profile
        if (!userService.userDetailsAuthentication(requesterId, userId, authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Extract token
        String token = extractToken(authentication);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Get details
        PatientProfileDetailsDTO userDetails = userService.getPatientDetails(
                userId, token, authentication);

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.ok(userDetails);
    }

    // Get user by username
    @GetMapping("/username/{username}")
    public ResponseEntity<String> getUserByUsername(@PathVariable String username) {
        User user = userService.findByUsername(username);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(user.getUserId());
    }

    // Get user by userId
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('Patient') or hasRole('Doctor') or hasRole('Other_Staff')")
    public ResponseEntity<UserDTO> getUserById(Authentication authentication, @PathVariable String userId) {
        UserDTO userDTO = userService.getUserDTOByUserId(userId, authentication);
        if (userDTO == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(userDTO);
    }

    private String extractToken(Authentication authentication) {
        if (authentication != null && authentication.getCredentials() instanceof Jwt) {
            Jwt jwt = (Jwt) authentication.getCredentials();
            return jwt.getTokenValue();
        }
        return null;
    }
}