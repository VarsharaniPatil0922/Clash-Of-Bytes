package com.clashofbytes.service;

import com.clashofbytes.dto.AuthResponse;
import com.clashofbytes.dto.LoginRequest;
import com.clashofbytes.dto.RegisterRequest;
import com.clashofbytes.entity.User;
import com.clashofbytes.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new IllegalArgumentException("Username '" + req.getUsername() + "' is already taken.");
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email '" + req.getEmail() + "' is already registered.");
        }

        String hashedPassword = passwordEncoder.encode(req.getPassword());
        User user = new User(req.getUsername(), req.getEmail(), hashedPassword);
        user.setCreatedAt(LocalDateTime.now());
        User saved = userRepository.save(user);

        String token = generateToken(saved);
        return new AuthResponse(
                true,
                "User registered successfully.",
                token,
                new AuthResponse.UserDto(saved.getUserId(), saved.getUsername(), saved.getEmail())
        );
    }

    public AuthResponse login(LoginRequest req) {
        Optional<User> userOpt = userRepository.findByUsernameOrEmail(req.getUsername(), req.getUsername());
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid username or password.");
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid username or password.");
        }

        String token = generateToken(user);
        return new AuthResponse(
                true,
                "Authentication successful.",
                token,
                new AuthResponse.UserDto(user.getUserId(), user.getUsername(), user.getEmail())
        );
    }

    public AuthResponse.UserDto getUserDto(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        return new AuthResponse.UserDto(user.getUserId(), user.getUsername(), user.getEmail());
    }

    private String generateToken(User user) {
        // Generates cryptographically secure session token
        return "cob_token_" + user.getUserId() + "_" + UUID.randomUUID().toString().replace("-", "");
    }
}
