package com.example.hotel_management_system.dto;

import lombok.Data;

@Data
public class UserRegistrationDTO {
    private Long userId;
    private String username;
    private String email;
    private String password;
    private String role;
    private Long roleId;
    private String firstName;
    private String lastName;
}