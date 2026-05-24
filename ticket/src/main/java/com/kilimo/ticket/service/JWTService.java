package com.kilimo.ticket.service;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JWTService {

    @Value("${jwt.secret}")
    private String secretkey;

    // public JWTService(){
    //     try {
    //         KeyGenerator keygen=KeyGenerator.getInstance("HmacSHA256");
    //         SecretKey sk=keygen.generateKey();
    //         secretkey=Base64.getEncoder().encodeToString(sk.getEncoded());
    //     } catch (NoSuchAlgorithmException e) {
    //         // TODO Auto-generated catch block
    //         e.printStackTrace();
    //     }
    // }
      
    public String generateToken(String username) {

        
       
        Map<String, Object> claims=new HashMap<>();
        return Jwts.builder()
                    .claims()
                    .add(claims)
                    .subject(username)
                    .issuedAt(new Date(System.currentTimeMillis()))
                    .expiration(new Date(System.currentTimeMillis() + 5 * 30 *60 *1000))
                    .and()
                    .signWith(getKey())
                    .compact();
    }

    private SecretKey getKey() {
        byte[] keybytes=secretkey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keybytes);
    }
    public String extractUsername(String token){
        return extractClaim(token, Claims::getSubject);
    }
    private <T> T extractClaim(String token, Function<Claims, T> claimResolver){
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }
    private Claims extractAllClaims(String token){
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
       try {
           final String userName=extractUsername(token);
           return (userName.equals(userDetails.getUsername()) && !isTokenExpired(token));
       } catch (JwtException | IllegalArgumentException ex) {
           return false;
       }
    }
    private boolean isTokenExpired(String token){
        return extractExpiration(token).before(new Date());
    }
    private Date extractExpiration(String token){
        return extractClaim(token, Claims::getExpiration);
    }

   

}
