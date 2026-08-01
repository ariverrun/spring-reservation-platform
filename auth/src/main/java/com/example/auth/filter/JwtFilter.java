package com.example.auth.filter;

import com.example.auth.service.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private static final int BEARER_PREFIX_LENGTH = BEARER_PREFIX.length();

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        String token = extractToken(request);

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!authenticateUser(token, request)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            return null;
        }

        return authHeader.substring(BEARER_PREFIX_LENGTH);
    }

    private boolean authenticateUser(String token, HttpServletRequest request) {
        try {
            Claims claims = jwtService.validateToken(token);
            UsernamePasswordAuthenticationToken authToken = createAuthenticationToken(claims, request);
            SecurityContextHolder.getContext().setAuthentication(authToken);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private UsernamePasswordAuthenticationToken createAuthenticationToken(Claims claims, HttpServletRequest request) {
        String userId = claims.getSubject();
        List<String> roles = claims.get("roles", List.class);

        List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userId,
                null,
                authorities
        );

        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        return authToken;
    }
}