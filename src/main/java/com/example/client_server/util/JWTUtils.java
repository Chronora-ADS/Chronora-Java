package com.example.client_server.util;

import com.example.client_server.model.entity.UserEntity;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.Date;

@Component
public class JWTUtils {
    private final String secret = "segredo-muito-fortissimo-muito-muito-muito-muito-muito-muito-muito-muito-muito-muito-muito-muito-muito-muito-muito-muito-mesmo";
    private int expiration = 86400000; // 24h

    private final JWTBlacklist blacklist;

    public JWTUtils(JWTBlacklist blacklist) {
        this.blacklist = blacklist;
    }

    public String generateToken(UserEntity user) {
        return Jwts.builder()
                .setSubject(user.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes()), SignatureAlgorithm.HS512)
                .compact();
    }

    public String getEmailFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean validateToken(String token) {
        if (blacklist.contains(token)) {
            return false; // token na blacklist
        }
        try {
            Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes()))
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException ex) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isTokenExpired(String token) {
        return parseClaims(token).getExpiration().before(new Date());
    }

    @Scheduled(fixedRate = 3600000) // A cada hora
    public void cleanUpExpiredTokens() {
        for (String token : blacklist.getAllTokens()) {
            try {
                Jws<Claims> claimsJws = Jwts.parserBuilder()
                        .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes()))
                        .build()
                        .parseClaimsJws(token);

                Claims claims = claimsJws.getBody();

                if (claims.getExpiration().before(new Date())) {
                    blacklist.removeToken(token);
                }
            } catch (JwtException | IllegalArgumentException e) {
                blacklist.removeToken(token);
            }
        }
    }
}
