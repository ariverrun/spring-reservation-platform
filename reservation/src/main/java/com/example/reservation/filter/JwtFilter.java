package com.example.reservation.filter;

import com.example.reservation.dto.UserInfo;
import com.example.reservation.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

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

        try {
            authenticateUser(token);
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            return null;
        }
        
        return authHeader.substring(BEARER_PREFIX_LENGTH);
    }

    private void authenticateUser(String token) throws Exception {
        UserInfo userInfo = buildUserInfo(token);
        UsernamePasswordAuthenticationToken authToken = createAuthenticationToken(userInfo);
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    private UserInfo buildUserInfo(String token) throws Exception {
        String userId = jwtService.extractUserId(token);
        List<String> roles = jwtService.extractRoles(token);

        return UserInfo.builder()
                .id(UUID.fromString(userId))
                .roles(roles)
                .build();
    }

    private UsernamePasswordAuthenticationToken createAuthenticationToken(UserInfo userInfo) {
        List<SimpleGrantedAuthority> authorities = userInfo.getRoles().stream()
                .map(SimpleGrantedAuthority::new)
                .toList();

        return new UsernamePasswordAuthenticationToken(
                userInfo,
                null,
                authorities
        );
    }
}