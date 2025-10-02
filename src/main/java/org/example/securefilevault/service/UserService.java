package org.example.securefilevault.service;

import jakarta.transaction.Transactional;
import org.example.securefilevault.Enum.Role;
import org.example.securefilevault.model.User;
import org.example.securefilevault.repository.FileMetadataRepository;
import org.example.securefilevault.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {
    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;


    public UserService(UserRepository userRepository, PasswordEncoder password){
        this.userRepository         = userRepository;
        this.passwordEncoder = password;
    }

    // Get all users
    public List<User> findAllUsers(){
        return userRepository.findAll();
    }

    // Get user by id
    public Optional<User> findUserById(Long id){
        return userRepository.findById(id);
    }

    //Find user by name
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // Find user by email
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    //Register a user
    public User registerUser(String username, String email, String password){
        if(userRepository.existsByUsername(username)){
            throw new RuntimeException("Username is already taken");
        }

        if(userRepository.existsByEmail(email)){
            throw new RuntimeException("Email is already taken");

        }

        // Create user obj to save user
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setPassword(passwordEncoder.encode(password));

        newUser.setRole(Role.USER);

        return userRepository.save(newUser);
    }




}
