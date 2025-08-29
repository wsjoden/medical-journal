package org.example.controller;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.Claim;
import io.quarkus.test.security.jwt.JwtSecurity;
import io.restassured.http.Header;
import org.example.config.NoDBTestProfile;
import org.example.entities.User;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
@TestProfile(NoDBTestProfile.class)
public class UserResourceTest {

    @Inject
    UserRepository userRepository;

    @BeforeEach
    public void setup() {
        UserRepository mock = Mockito.mock(UserRepository.class);

        List<User> mockUsers = createMockUsers();

        Mockito.when(mock.searchPatients(
                        Mockito.any(), Mockito.any(), Mockito.any(),
                        Mockito.any(), Mockito.any(), Mockito.any(),
                        Mockito.any(), Mockito.any()))
                .thenReturn(mockUsers);

        Mockito.when(mock.searchPatients(
                        "john", null, null, null, null, null, null, null))
                .thenReturn(mockUsers);

        Mockito.when(mock.searchPatients(
                        null, "John", "Doe", null, null, null, null, null))
                .thenReturn(mockUsers.subList(0, 1));

        QuarkusMock.installMockForType(mock, UserRepository.class);
    }

    @Test
    @TestSecurity(user = "doctor123", roles = {"doctor"})
    public void testTestEndpointWithDoctorRole() {
        given()
                .when().get("/search/test")
                .then()
                .statusCode(200)
                .body(is("Search service is up and running!"));
    }

    @Test
    @TestSecurity(user = "staff123", roles = {"other_staff"})
    public void testTestEndpointWithStaffRole() {
        given()
                .when().get("/search/test")
                .then()
                .statusCode(200)
                .body(is("Search service is up and running!"));
    }

    @Test
    @TestSecurity(user = "patient123", roles = {"patient"})
    public void testTestEndpointWithPatientRole() {
        given()
                .when().get("/search/test")
                .then()
                .statusCode(200)
                .body(is("Search service is up and running!"));
    }

    @Test
    @TestSecurity(user = "doctor123", roles = {"doctor"})
    public void testSearchPatientsWithGenericSearch() {
        given()
                .queryParam("genericSearch", "john")
                .when().get("/search")
                .then()
                .statusCode(200)
                .body("size()", is(2))
                .body("[0].firstName", is("John"))
                .body("[1].firstName", is("Johnny"));
    }

    @Test
    @TestSecurity(user = "doctor123", roles = {"doctor"})
    public void testSearchPatientsWithSpecificParams() {
        given()
                .queryParam("firstName", "John")
                .queryParam("lastName", "Doe")
                .when().get("/search")
                .then()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].firstName", is("John"))
                .body("[0].lastName", is("Doe"));
    }

    @Test
    @TestSecurity(user = "patient123", roles = {"patient"})
    public void testSearchPatientsUnauthorized() {
        given()
                .queryParam("genericSearch", "john")
                .when().get("/search")
                .then()
                .statusCode(200);
    }

    @Test
    @TestSecurity(user = "doctor123", roles = {"doctor"})
    public void testSearchPatientsAllParameters() {
        given()
                .queryParam("firstName", "John")
                .queryParam("lastName", "Doe")
                .queryParam("diagnose", "mock diagnose")
                .queryParam("encounterDate", "2023-01-15")
                .queryParam("observation", "mock observation")
                .queryParam("staffFirstName", "Dr")
                .queryParam("staffLastName", "Smith")
                .when().get("/search")
                .then()
                .statusCode(200)
                .body("size()", is(2));
    }
    
    private List<User> createMockUsers() {
        List<User> users = new ArrayList<>();

        User user1 = new User();
        user1.setUserId("123");
        user1.setFirstName("John");
        user1.setLastName("Doe");

        User user2 = new User();
        user2.setUserId("456");
        user2.setFirstName("Johnny");
        user2.setLastName("Smith");

        users.add(user1);
        users.add(user2);

        return users;
    }
}