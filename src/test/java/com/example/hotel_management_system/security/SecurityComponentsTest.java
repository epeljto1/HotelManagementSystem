package com.example.hotel_management_system.security;

import com.example.hotel_management_system.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SecurityComponentsTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void tokenBlacklistTracksRevokedTokens() {
        TokenBlacklist tokenBlacklist = new TokenBlacklist();

        tokenBlacklist.add("jwt-1");

        assertTrue(tokenBlacklist.isBlacklisted("jwt-1"));
        assertFalse(tokenBlacklist.isBlacklisted("jwt-2"));
    }

    @Test
    void jwtAuthenticationFilterPassesThroughWhenHeaderIsMissing() throws Exception {
        JwtUtil jwtUtil = mock(JwtUtil.class);
        TokenBlacklist tokenBlacklist = mock(TokenBlacklist.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtUtil, tokenBlacklist);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void jwtAuthenticationFilterRejectsBlacklistedToken() throws Exception {
        JwtUtil jwtUtil = mock(JwtUtil.class);
        TokenBlacklist tokenBlacklist = mock(TokenBlacklist.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtUtil, tokenBlacklist);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        StringWriter writer = new StringWriter();

        when(request.getHeader("Authorization")).thenReturn("Bearer blocked-token");
        when(tokenBlacklist.isBlacklisted("blocked-token")).thenReturn(true);
        when(response.getWriter()).thenReturn(new PrintWriter(writer));

        filter.doFilterInternal(request, response, chain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(chain, never()).doFilter(request, response);
        assertTrue(writer.toString().contains("Token je ponisten"));
    }

    @Test
    void jwtAuthenticationFilterAuthenticatesValidToken() throws Exception {
        JwtUtil jwtUtil = mock(JwtUtil.class);
        TokenBlacklist tokenBlacklist = mock(TokenBlacklist.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtUtil, tokenBlacklist);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(request.getHeader("Authorization")).thenReturn("Bearer good-token");
        when(tokenBlacklist.isBlacklisted("good-token")).thenReturn(false);
        when(jwtUtil.isTokenValid("good-token")).thenReturn(true);
        when(jwtUtil.extractUsername("good-token")).thenReturn("amina");
        when(jwtUtil.extractRole("good-token")).thenReturn("ADMIN");

        filter.doFilterInternal(request, response, chain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals("amina", authentication.getName());
        assertEquals(List.of("ROLE_ADMIN"),
                authentication.getAuthorities().stream().map(Object::toString).toList());
        verify(chain).doFilter(request, response);
    }
}
