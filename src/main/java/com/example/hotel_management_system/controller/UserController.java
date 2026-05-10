package com.example.hotel_management_system.controller;

import com.example.hotel_management_system.dto.UserRegistrationDTO;
import com.example.hotel_management_system.service.UserService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody UserRegistrationDTO dto) {
        try {
            userService.register(dto);
            return ResponseEntity.ok("Korisnik uspješno registrovan!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Greška pri registraciji: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            String token = userService.login(loginRequest.getUsername(), loginRequest.getPassword());
            return ResponseEntity.ok(new AuthResponse(token));
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Greška pri prijavi: " + e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                userService.logout(token);
                return ResponseEntity.ok("Uspješna odjava. Token je poništen.");
            }
            return ResponseEntity.badRequest().body("Token nedostaje u Authorization zaglavlju.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Greška pri odjavi: " + e.getMessage());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllUsers() {
        try {
            return ResponseEntity.ok(userService.getAllUsers());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Greška: " + e.getMessage());
        }
    }
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {
        private String username;
        private String password;
    }

    @Data
    @AllArgsConstructor
    public static class AuthResponse {
        private String token;
    }
}
