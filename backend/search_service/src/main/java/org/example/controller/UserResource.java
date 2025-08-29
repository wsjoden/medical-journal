package org.example.controller;

import java.util.Base64;
import java.util.List;
import java.util.Map;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import org.example.repository.UserRepository;
import org.jose4j.jwt.JwtClaims;
import org.example.entities.User;
import org.eclipse.microprofile.jwt.JsonWebToken;

import io.smallrye.jwt.auth.principal.DefaultJWTCallerPrincipal;
import io.smallrye.jwt.build.JwtClaimsBuilder;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

@Path("/search")
public class UserResource {

    @Inject
    UserRepository userRepository;

    @GET
    @Path("/test")
    public String testSearchService(@Context HttpHeaders headers) {
        System.out.println("Search service is up and running!");

        String token = null;

        for (Map.Entry<String, java.util.List<String>> entry : headers.getRequestHeaders().entrySet()) {
            // System.out.println(entry.getKey() + ": " + String.join(", ",
            // entry.getValue()));

            if ("Authorization".equalsIgnoreCase(entry.getKey())) {
                token = entry.getValue().get(0); // Get the first value
                if (token.startsWith("Bearer ")) {
                    token = token.substring(7); // Remove "Bearer " prefix
                }
            }
        }

        System.out.println("Extracted Token: " + token);

        if (token != null) {
            try {
                // Decode JWT claims using SmallRye JWT
                JwtClaims claims = JwtClaims.parse(decodeJWT(token));

                System.out.println("Role: " + claims.getClaimValue("role"));
                if(!claims.getClaimValue("role").equals("doctor") && !claims.getClaimValue("role").equals("other_staff")){
                    return "Access Denied";
                }
            } catch (Exception e) {
                System.err.println("Failed to decode JWT: " + e.getMessage());
            }
        }

        return "Search service is up and running!";
    }

    private String decodeJWT(String token) {
        // JWT format: header.payload.signature
        String[] parts = token.split("\\.");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid JWT token format");
        }

        // Decode payload (Base64URL)
        return new String(Base64.getUrlDecoder().decode(parts[1]));
    }

    @GET
    public Uni<Response> searchPatients(@Context HttpHeaders headers,
            @QueryParam("genericSearch") String genericSearch,
            @QueryParam("firstName") String firstName,
            @QueryParam("lastName") String lastName,
            @QueryParam("diagnose") String diagnose,
            @QueryParam("encounterDate") String encounterDate,
            @QueryParam("observation") String observation,
            @QueryParam("staffFirstName") String staffFirstName,
            @QueryParam("staffLastName") String staffLastName) {

        String token = null;

        for (Map.Entry<String, java.util.List<String>> entry : headers.getRequestHeaders().entrySet()) {
            // System.out.println(entry.getKey() + ": " + String.join(", ",
            // entry.getValue()));

            if ("Authorization".equalsIgnoreCase(entry.getKey())) {
                token = entry.getValue().get(0); // Get the first value
                if (token.startsWith("Bearer ")) {
                    token = token.substring(7); // Remove "Bearer " prefix
                }
            }
        }

        System.out.println("Extracted Token: " + token);

        if (token != null) {
            try {
                // Decode JWT claims using SmallRye JWT
                JwtClaims claims = JwtClaims.parse(decodeJWT(token));

                System.out.println("Role: " + claims.getClaimValue("role"));
                
                if(!claims.getClaimValue("role").equals("doctor") && !claims.getClaimValue("role").equals("other_staff")){
                    return Uni.createFrom().item(() -> Response.status(Response.Status.FORBIDDEN).build());
                }
            } catch (Exception e) {
                System.err.println("Failed to decode JWT: " + e.getMessage());
            }
        }

    

        return Uni.createFrom().item(() -> {
            if (genericSearch != null && !genericSearch.isBlank()) {
                System.out.println("Performing a generic search with query: " + genericSearch);
            } else {
                System.out.println("Performing a parameterized search with the following parameters:");
                if (firstName != null)
                    System.out.println("First Name: " + firstName);
                if (lastName != null)
                    System.out.println("Last Name: " + lastName);
                if (diagnose != null)
                    System.out.println("Diagnose: " + diagnose);
                if (encounterDate != null)
                    System.out.println("Encounter Date: " + encounterDate);
                if (observation != null)
                    System.out.println("Observation: " + observation);
                if (staffFirstName != null)
                    System.out.println("Staff First Name: " + staffFirstName);
                if (staffLastName != null)
                    System.out.println("Staff Last Name: " + staffLastName);
            }

            List<User> patients = userRepository.searchPatients(
                    genericSearch,
                    firstName,
                    lastName,
                    diagnose,
                    encounterDate,
                    observation,
                    staffFirstName,
                    staffLastName);
            return Response.ok(patients).build();
        });
    }

}
