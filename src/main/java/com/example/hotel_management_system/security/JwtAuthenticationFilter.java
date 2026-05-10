package com.example.hotel_management_system.security;

import com.example.hotel_management_system.config.DbConfig;
import com.example.hotel_management_system.repository.JwtTokenRepository;
import com.example.hotel_management_system.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final JwtTokenRepository jwtTokenRepository;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, JwtTokenRepository jwtTokenRepository) {
        this.jwtUtil = jwtUtil;
        this.jwtTokenRepository = jwtTokenRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // AKO NEMA TOKENA, SAMO PROSLIJEDI DALJE (Spring Security će odlučiti o pristupu)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        if (!isTokenActive(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token nije aktivan. Molimo prijavite se ponovo.");
            return;
        }

        // VALIDACIJA TOKENA
        if (jwtUtil.isTokenValid(token)) {
            String username = jwtUtil.extractUsername(token);
            String role = jwtUtil.extractRole(token);

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    username, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role)));

            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        filterChain.doFilter(request, response);
    }

    private boolean isTokenActive(String token) {
        try (Connection conn = DbConfig.getConnection()) {
            return jwtTokenRepository.existsActiveToken(token, conn);
        } catch (SQLException e) {
            return false;
        }
    }
}
