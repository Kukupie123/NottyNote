package kuku.advbkm.gateway.configs.Security;

import kuku.advbkm.gateway.service.JWTService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/*
Note to self : Understand Map and FlatMap properly. They are so confusing
 */

/**
 * Security Note 4 :
 * Authenticates the BearerToken returned from AuthConverter inside the "authenticate" function and returns a UsernamePasswordAuthenticationToken
 * This function is also going to be using JWTService to validate and get the username
 */
@Component
public class AuthManager implements ReactiveAuthenticationManager {
    @Qualifier("dummy_userDetails")
    final UserDetails dummyUserDetails;
    final
    JWTService jwtService; //Add final keyword, or they are not taken into consideration during dependency injection
    final MongoUSerDetailService mongoUserDetailService;

    public AuthManager(UserDetails dummyUserDetails, JWTService jwtService, MongoUSerDetailService mongoUserDetailService) {
        this.dummyUserDetails = dummyUserDetails;
        this.jwtService = jwtService;
        this.mongoUserDetailService = mongoUserDetailService;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        //Return a mono OR Empty
        return Mono.justOrEmpty(
                        authentication
                )
                //Here begins the step of making changes to the mono object, firstly we cast it to BearerToken
                .cast(BearerToken.class)
                //Then we are going to transform/change/mutate the authentication object. Here, We will validate in our own way and return a Mono<Authentication>
                .flatMap(auth -> { //Why did we use flatmap? Because if we don't the return type becomes Mono<Mono<Object>> instead of Mono<Authentication> as Map covers the return type with a Mono
                    String userName = jwtService.getUserName(auth.getCredentials());
                    Mono<UserDetails> user = mongoUserDetailService.findByUsername(userName).defaultIfEmpty(dummyUserDetails);

                    //Why do we use flatmap? Because If we map to transform an object we can't return Mono.error as this will change the type to Mono<Mono<Exception>>
                    //But we need Mono<Authentication>
                    Mono<Authentication> userToAuthMap = user.flatMap(u -> {
                        //Check if user is valid
                        if (u.getUsername() == null) {
                            return Mono.error(new Exception("User not found"));
                        }

                        //validate token
                        if (jwtService.isValid(auth.getCredentials(), u.getUsername())) {
                            return Mono.just(new UsernamePasswordAuthenticationToken(u.getUsername(), u.getPassword(), u.getAuthorities()));
                        }
                        return Mono.error(new Exception("Invalid/Expired Token"));
                    });

                    return userToAuthMap;
                })
                //Although It  Should never happen because we throw error but just adding as extra safety check
                .switchIfEmpty(Mono.error(new Exception("No Auth Header Found")))
                //Finally, if we catch any error, which we will if auth fails we will throw a general exception along with the message
                .onErrorResume(e -> Mono.error(new Exception(e.getMessage())));
    }

}
