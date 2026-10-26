package com.iasc;
import io.jsonwebtoken.*; import io.jsonwebtoken.security.Keys; import org.springframework.beans.factory.annotation.Value; import org.springframework.stereotype.Component;
import javax.crypto.SecretKey; import java.util.Date;
@Component
public class JwtService {
 private final SecretKey key;
 public JwtService(@Value("${app.jwt-secret}") String s){ key=Keys.hmacShaKeyFor(s.getBytes()); }
 public String make(User u){ return Jwts.builder().subject(u.getEmail()).expiration(new Date(System.currentTimeMillis()+7*86400000L)).signWith(key).compact(); }
 public String parse(String t){ return Jwts.parser().verifyWith(key).build().parseSignedClaims(t).getPayload().getSubject(); }
}
