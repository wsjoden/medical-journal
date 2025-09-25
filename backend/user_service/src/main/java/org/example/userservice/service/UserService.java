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
    // return userRepository.findById(userId);
    // }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public List<User> findAllPatients() {
        return userRepository.findAllPatients();
    }

    public User authenticateUser(String username, String password) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'authenticateUser'");
    }
}
