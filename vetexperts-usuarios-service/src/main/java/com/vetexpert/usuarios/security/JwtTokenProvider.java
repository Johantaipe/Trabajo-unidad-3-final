package com.vetexpert.usuarios.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Proveedor de tokens JWT: generación, validación y extracción de claims.
 */
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes();
        if (keyBytes.length < 32) {
            throw new IllegalStateException("JWT secret debe tener al menos 256 bits (32 caracteres)");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Genera un token JWT para el usuario autenticado.
     */
    public String generateToken(String username, String rol, Long userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .subject(username)
                .claim("rol", rol)
                .claim("userId", userId)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extrae el username del token.
     */
    public String getUsernameFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Extrae el rol del token.
     */
    public String getRolFromToken(String token) {
        return parseClaims(token).get("rol", String.class);
    }

    /**
     * Extrae el userId del token.
     */
    public Long getUserIdFromToken(String token) {
        return parseClaims(token).get("userId", Long.class);
    }

    /**
     * Valida que el token sea correcto y no esté expirado.
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            throw new IllegalArgumentException("Token JWT expirado");
        } catch (UnsupportedJwtException e) {
            throw new IllegalArgumentException("Token JWT no soportado");
        } catch (MalformedJwtException e) {
            throw new IllegalArgumentException("Token JWT malformado");
        } catch (SecurityException e) {
            throw new IllegalArgumentException("Firma JWT inválida");
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Token JWT vacío o nulo");
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
