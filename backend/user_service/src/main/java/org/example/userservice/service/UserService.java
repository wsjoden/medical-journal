package org.example.userservice.service;

import org.example.userservice.model.User;
import org.example.userservice.repository.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final IUserRepository userRepository;

    @Autowired
    public UserService(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User saveUser(User user) {
        System.out.println("Saving user: " + user.toString());
        return userRepository.save(user);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User findByUserId(String userId) {
        return userRepository.findByUserId(userId);
    }

    // public Optional<User> findById(Long userId) {
    //     return userRepository.findById(userId);
    // }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public List<User> findAllPatients() {
        return userRepository.findAllPatients();
    }

    // public List<User> findUserByRole(Role role) {
    //     return userRepository.findByRole(role);
    // }

    // public User updateUser(User user) {
    //     if (!userRepository.existsById(user.getId())) {
    //         throw new IllegalArgumentException("User does not exist");
    //     }
    //     return userRepository.save(user);
    // }

    // public void deleteUser(Long id) {
    //     User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User does not exist"));
    //     userRepository.deleteById(user.getId());
    // }


    public User authenticateUser(String username, String password) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'authenticateUser'");
    }

    // public void syncUser(Jwt jwt) {
    //     System.out.println("Syncing user");
    //     System.out.println("Claims: " + jwt.getClaims());
    //     String userId = jwt.getClaim("sub");
    //     String username = jwt.getClaim("preferred_username");
    //     String firstName = jwt.getClaim("given_name");
    //     String lastName = jwt.getClaim("family_name");
    //     String role = jwt.getClaim("role");

    //     if (!userRepository.existsById(userId)) {
    //         System.out.println("New user found! Adding to database");
    //         User user = new User();
    //         user.setUserId(userId);
    //         user.setUsername(username);
    //         user.setFirstName(firstName);
    //         user.setLastName(lastName);
    //         user.setRole(role);
    //         System.out.println("user: " + user.toString());
    //         userRepository.save(user);
    //     } else {
    //         User existingUser = userRepository.findById(userId).orElseThrow();
    //         if (!existingUser.getUsername().equals(username)) {
    //             existingUser.setUsername(username);
    //             userRepository.save(existingUser);
    //         } else if (!existingUser.getFirstName().equals(firstName)) {
    //             existingUser.setFirstName(firstName);
    //             userRepository.save(existingUser);
    //         } else if (!existingUser.getLastName().equals(lastName)) {
    //             existingUser.setLastName(lastName);
    //             userRepository.save(existingUser);
    //         }
    //     }
    // }
}
