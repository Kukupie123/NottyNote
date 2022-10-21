package kuku.advbkm.gateway.service;

import io.jsonwebtoken.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class JWTService {

    private String key;
    private JwtParser parser;

    public JWTService() {
        key = "1234567890";
        parser = Jwts.parser();
    }


    public String generate(String userName) {
        JwtBuilder builder = Jwts.builder()
                .setSubject(userName)
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plus(15, ChronoUnit.MINUTES)))
                .signWith(SignatureAlgorithm.HS256,key);


        String token = builder.compact();

        return token;

    }

    public String getUserName(String token) {
        //Subject is where we store the username
        return parser
                .setSigningKey(key)
                .parseClaimsJws(token)
                .getBody().getSubject();


    }

    public boolean isValid(String token, UserDetails user) {
        Claims claims = parser
                .setSigningKey(key)
                .parseClaimsJws(token).
                getBody();
        boolean unexpired = claims.getExpiration().after(Date.from(Instant.now()));

        return unexpired && user.getUsername() == claims.getSubject();
    }
}
