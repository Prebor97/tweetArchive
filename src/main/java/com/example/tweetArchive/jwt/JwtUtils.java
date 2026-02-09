package com.example.tweetArchive.jwt;

import com.example.tweetArchive.entities.UserInfo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.*;

@Component
public class JwtUtils {

    @Value("${JwtsecretKey}")
    private String secretKey;

    public String generateToken(UserInfo user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId",user.getUserId());
        claims.put("twitter_username", user.getTwitterUserName());
        String token = Jwts.
                builder()
                .claims(claims)
                .subject(user.getEmail())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 3 * 60 * 60 * 1000))
                .issuer("Prebor")
                .signWith(generateKey())
                .compact();
        return token;
    }

    private SecretKey generateKey() {
        byte[] keyBytes = Decoders.BASE64URL.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(generateKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    public String getUserName(String token){
        Claims claims = extractAllClaims(token);
        return claims.getSubject();
    }

    public List<String> getUserInfo(String token){
        Claims claims = extractAllClaims(token);
        List<String> userDetails = new ArrayList<>();
        userDetails.add(0,claims.get("userId", String.class));
        userDetails.add(1,claims.get("twitter_username", String.class));
        return userDetails;
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        Claims claims = extractAllClaims(token);
        return !claims.getExpiration().before(new Date());
    }
}

