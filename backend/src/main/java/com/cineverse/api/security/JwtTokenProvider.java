package com.cineverse.api.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtTokenProvider {


    private final String SECRET_STRING = "EstaEsUnaClaveSuperSecretaYMuyLargaParaCumplirConLosRequisitosDeSeguridad512Bits!";
    private final SecretKey JWT_SECRET = Keys.hmacShaKeyFor(SECRET_STRING.getBytes(StandardCharsets.UTF_8));
    private final long JWT_EXPIRATION = 86400000L; // 1 día

    /**
     * Genera el token asegurando que el Subject y los Claims coexistan.
     */
    public String generateToken(String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION))
                .signWith(JWT_SECRET, SignatureAlgorithm.HS512)
                .compact();
    }

    public boolean isTokenValid(String token, String username) {
        try {
            final String tokenUsername = getUsernameFromJWT(token);
            return (tokenUsername.equals(username) && !isTokenExpired(token));
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    public String getUsernameFromJWT(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrae el rol específicamente.
     * Si no existe, devuelve "USER" por defecto para evitar punteros nulos.
     */
    public String getRoleFromJWT(String token) {
        String role = extractClaim(token, claims -> claims.get("role", String.class));
        return (role != null) ? role : "USER";
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(JWT_SECRET)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}