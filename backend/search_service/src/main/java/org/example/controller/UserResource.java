package org.example.controller;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;

import org.example.entities.User;
import org.example.repository.UserRepository;
import org.jose4j.jwt.JwtClaims;
import org.eclipse.microprofile.jwt.JsonWebToken;

import io.quarkus.vertx.http.runtime.devmode.Json;
import io.smallrye.mutiny.Uni;
import java.util.List;
import java.util.Map;

@Path("/search")
public class UserResource {

    @Inject
    UserRepository userRepository;

    @Inject
    JsonWebToken jwt;

    @GET
    @Path("/test")
    // @PermitAll disabled for testing.
    @RolesAllowed({ "doctor", "other_staff" })
    public String testSearchService(@Context HttpHeaders headers) {
        System.out.println("Search service is up and running!");
        return "Search service is up and running!";
    }

    @GET
    @RolesAllowed({ "doctor", "other_staff" })
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
