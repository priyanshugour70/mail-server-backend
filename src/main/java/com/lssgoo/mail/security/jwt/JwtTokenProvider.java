package com.lssgoo.mail.security.jwt;

import com.lssgoo.mail.utils.LoggerUtil;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerUtil.getLogger(JwtTokenProvider.class);

    @Value("${jwt.secret:your-secret-key-must-be-at-least-256-bits-long-for-hs256-algorithm}")
    private String jwtSecret;

    @Value("${jwt.access-token-expiration:3600000}") // 1 hour default
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration:604800000}") // 7 days default
    private long refreshTokenExpiration;

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(Long userId, String username, Long sessionId) {
        logger.debug("Generating access token for user: {} (ID: {}), session: {}", username, userId, sessionId);
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("sessionId", sessionId);
        claims.put("type", "access");
        return createToken(claims, username, accessTokenExpiration);
    }

    public String generateRefreshToken(Long userId, String username, Long sessionId) {
        logger.debug("Generating refresh token for user: {} (ID: {}), session: {}", username, userId, sessionId);
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("sessionId", sessionId);
        claims.put("type", "refresh");
        return createToken(claims, username, refreshTokenExpiration);
    }

    private String createToken(Map<String, Object> claims, String subject, long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        Object userId = claims.get("userId");
        if (userId instanceof Integer) {
            return ((Integer) userId).longValue();
        }
        return (Long) userId;
    }

    public Long getSessionIdFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        Object sessionId = claims.get("sessionId");
        if (sessionId == null) {
            return null;
        }
        if (sessionId instanceof Integer) {
            return ((Integer) sessionId).longValue();
        }
        return (Long) sessionId;
    }

    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public String getTokenType(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return (String) claims.get("type");
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            logger.warn("JWT token is expired");
            throw new RuntimeException("JWT token is expired", e);
        } catch (UnsupportedJwtException e) {
            logger.warn("JWT token is unsupported");
            throw new RuntimeException("JWT token is unsupported", e);
        } catch (MalformedJwtException e) {
            logger.warn("Invalid JWT token format");
            throw new RuntimeException("Invalid JWT token", e);
        } catch (IllegalArgumentException e) {
            logger.warn("JWT claims string is empty");
            throw new RuntimeException("JWT claims string is empty", e);
        }
    }

    public Boolean validateToken(String token) {
        try {
            getAllClaimsFromToken(token);
            return true;
        } catch (Exception e) {
            logger.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public Boolean isTokenExpired(String token) {
        try {
            final Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    public long getAccessTokenExpirationInMillis() {
        return accessTokenExpiration;
    }

    public long getRefreshTokenExpirationInMillis() {
        return refreshTokenExpiration;
    }
}

