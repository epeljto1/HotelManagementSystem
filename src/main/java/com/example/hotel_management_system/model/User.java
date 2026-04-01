package com.example.hotel_management_system.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private Long id;
    private Long userId;
    private Long roleId;
    private String username;
    private String email;
    private String passwordHash;
    private String role;
    private LocalDate createdDate;
    private String firstName;
    private String lastName;
}