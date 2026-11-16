package com.clashofbytes.controller;

import com.clashofbytes.dto.AuthResponse;
import com.clashofbytes.dto.LoginRequest;
import com.clashofbytes.dto.RegisterRequest;
import com.clashofbytes.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        AuthResponse response = authService.register(req);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        AuthResponse response = authService.login(req);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse.UserDto> getMe(@RequestParam(name = "userId", defaultValue = "1") Long userId) {
        AuthResponse.UserDto user = authService.getUserDto(userId);
        return ResponseEntity.ok(user);
    }
}
