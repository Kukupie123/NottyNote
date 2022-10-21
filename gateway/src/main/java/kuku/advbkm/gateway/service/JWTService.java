package kuku.advbkm.gateway.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class JWTService {

    final private SecretKey key;
    final private JwtParser parser;

    public JWTService() {
        key = Keys.hmacShaKeyFor("Keys.hmacShaKeyFor(\"1234567890\".getBytes(StandardCharsets.UTF_8));".getBytes(StandardCharsets.UTF_8));
        parser = Jwts.parserBuilder().setSigningKey(key).build();
    }


    public String generate(String userName) {
        JwtBuilder builder = Jwts.builder()
                .setSubject(userName)
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plus(15, ChronoUnit.MINUTES)))
                .signWith(key);


        return builder.compact();

    }

    public String getUserName(String token) {
        //Subject is where we store the username
        var a = parser
                .parseClaimsJws(token)
                .getBody().getSubject();
        return a;


    }

    public boolean isValid(String token, UserDetails user) {
        Claims claims = parser
                .parseClaimsJws(token).
                getBody();
        boolean unexpired = claims.getExpiration().after(Date.from(Instant.now()));


        return unexpired && user.getUsername().equals(claims.getSubject());
    }
}
