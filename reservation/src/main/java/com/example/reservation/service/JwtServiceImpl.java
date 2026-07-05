package com.example.reservation.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JwtServiceImpl implements JwtService {

    @Value("${jwt.public-key-path}")
    private String publicKeyPath;

    private PublicKey getPublicKey() throws Exception {
        try (BufferedReader reader = new BufferedReader(new FileReader(publicKeyPath))) {
            String content = reader.lines()
                    .collect(Collectors.joining("\n"))
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] keyBytes = Base64.getDecoder().decode(content);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(spec);
        }
    }

    @Override
    public Claims validateToken(String token) throws Exception {
        return Jwts.parserBuilder()
                .setSigningKey(getPublicKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    @Override
    public String extractUserId(String token) throws Exception {
        return validateToken(token).getSubject();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) throws Exception {
        return (List<String>) validateToken(token).get("roles", List.class);
    }
}