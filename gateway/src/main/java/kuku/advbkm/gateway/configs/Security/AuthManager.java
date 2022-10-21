package kuku.advbkm.gateway.configs.Security;

import kuku.advbkm.gateway.service.JWTService;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class AuthManager implements ReactiveAuthenticationManager {
    final
    JWTService jwtService; //Add final keyword, or they are not taken into consideration during dependency injection
    final ReactiveUserDetailsService users;

    public AuthManager(JWTService jwtService, ReactiveUserDetailsService users) {
        this.jwtService = jwtService;
        this.users = users;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        return Mono.justOrEmpty(
                        authentication
                )
                .cast(BearerToken.class)
                .map(auth -> validate(auth))
                .onErrorMap(err -> new AuthenticationException(err.getMessage()) {
                    @Override
                    public String getMessage() {
                        return err.getMessage();
                    }
                });
    }

    private Authentication validate(BearerToken token) {
        String userName = jwtService.getUserName(token.getCredentials()); //gets us the username, Check bearer token class to confirm
        Mono<UserDetails> user = users.findByUsername(userName);

        //We map when ever we want to get a value of a mono. We are casting the return value to authentication as it is what we get. We also throw exception if token is invalid
        return user.map(ud -> {
            //Check if credentials are valid
            if (jwtService.isValid(token.getCredentials(), ud)) {
                return new UsernamePasswordAuthenticationToken(ud.getUsername(), ud.getPassword(), ud.getAuthorities());
            }
            throw new IllegalArgumentException("Invalid Token");

        });

    }
}
