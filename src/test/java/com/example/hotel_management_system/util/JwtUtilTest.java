package com.example.hotel_management_system.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtUtilTest {

    private final JwtUtil jwtUtil = new JwtUtil();

    @Test
    void generateTokenIncludesExpectedClaims() {
        String token = jwtUtil.generateToken("receptionist", "ADMIN");

        assertTrue(jwtUtil.isTokenValid(token));
        assertEquals("receptionist", jwtUtil.extractUsername(token));
        assertEquals("ADMIN", jwtUtil.extractRole(token));
    }

    @Test
    void isTokenValidReturnsFalseForTamperedToken() {
        String token = jwtUtil.generateToken("receptionist", "ADMIN");
        String tamperedToken = token.substring(0, token.length() - 2) + "xx";

        assertFalse(jwtUtil.isTokenValid(tamperedToken));
    }
}
