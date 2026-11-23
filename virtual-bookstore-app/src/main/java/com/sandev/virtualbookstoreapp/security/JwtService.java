package com.sandev.virtualbookstoreapp.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {
    private static final String TOKEN_TYPE = "token_type";
    private final PublicKey publicKey;
    private final PrivateKey privateKey;
    @Value("${app.security.jwt.access-token-expiration}")
    private Long EXPIRATION_TIME;
    @Value("${app.security.jwt.refresh-token-expiration}")
    private Long REFRESH_TIME;

    public JwtService() throws Exception {
        this.publicKey = KeyUtils.loadPublicKey("keys/local-only/public-key.pem");
        this.privateKey = KeyUtils.loadPrivateKey("keys/local-only/private-key.pem");
    }

    public String generateAccessToken(final String username){
        final Map<String , Object> claims = Map.of(TOKEN_TYPE, "ACCESS_TOKEN");
        return buildToken(claims, username, EXPIRATION_TIME);
    }
    public String generateRefreshToken(final String username){
        final Map<String , Object> claims = Map.of(TOKEN_TYPE, "REFRESH_TOKEN");
        return buildToken(claims, username, REFRESH_TIME);
    }

    public String generateRefreshAccessToken(final String refreshToken){
        final Claims claims = extractClaims(refreshToken);
        if(!"REFRESH_TOKEN".equals(claims.get(TOKEN_TYPE))) {
            throw new JwtException("Invalid refresh token");
        }
        if(isTokenExpired(refreshToken)){
            throw new JwtException("Token is expired");
        }
        final String username = claims.getSubject();
        return generateAccessToken(username);
    }

    public String extractUsername(final String token){
        return extractClaims(token).getSubject();
    }

    public boolean isTokenValid(final String token, final String username){
        final String extractedUsername = extractClaims(token).getSubject();
        return extractedUsername.equals(username) && !isTokenExpired(token);
    }

    public boolean isTokenExpired(final String token){
        final Claims claims = extractClaims(token);
        return claims.getExpiration().before(new Date());
    }

    public Claims extractClaims(final String refreshToken) {
        try {
            return Jwts.parser()
                    .verifyWith(this.publicKey)
                    .build()
                    .parseSignedClaims(refreshToken)
                    .getPayload();
        } catch (final JwtException e) {
            throw new JwtException("Invalid refresh token", e);
        }
    }

    private String buildToken(final Map<String , Object> claims, final String username, final Long expirationTime){
        return Jwts.builder()
                .claims(claims)
                .signWith(this.privateKey)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .compact();
    }
}
