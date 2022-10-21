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

/*
Note to self : Understand Map and FlatMap properly. They are so confusing
 */
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
                .flatMap(auth -> validate(auth)) // I used map earlier and it didn't work well with validate function. As i failed to figure out how to return a non mono object in validate function. So I had to make it return Mono<Auth> instead of Auth
                .onErrorMap(err -> new AuthenticationException(err.getMessage()) {
                    @Override
                    public String getMessage() {
                        return err.getMessage();
                    }
                });
    }

    private Mono<Authentication> validate(BearerToken token) {
        String userName = jwtService.getUserName(token.getCredentials()); //gets us the username, Check bearer token class to confirm
        Mono<UserDetails> user = users.findByUsername(userName);
        //Originally I planned to return an UsernamePasswordAuthenticationToken object but it just wasn't working so I ended up changing the return type to Mono<Authentication>
        Mono<Authentication> result = user.map(ud -> {
            //Check if credentials are valid
            if (jwtService.isValid(token.getCredentials(), ud)) {
                return new UsernamePasswordAuthenticationToken(ud.getUsername(), ud.getPassword(), ud.getAuthorities());
            }
            throw new IllegalArgumentException("Invalid Token");

        });

        return result;

    }
}
