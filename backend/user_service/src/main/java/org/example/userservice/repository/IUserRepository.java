package org.example.userservice.repository;

import org.example.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface IUserRepository extends JpaRepository<User, String> {

    // find user by their username
    User findByUsername(String username);

    // find user by their userId
    User findByUserId(String userId);

    // find all user with role patient
    @Query("SELECT u FROM User u WHERE u.role = 'Patient'")
    List<User> findAllPatients();

}
