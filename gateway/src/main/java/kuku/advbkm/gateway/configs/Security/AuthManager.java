package kuku.advbkm.gateway.configs.Security;

import kuku.advbkm.gateway.service.JWTService;
import lombok.extern.log4j.Log4j2;
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
 * <p>
 * It also makes use of the MongoUserServiceDetails object to get the user from mongoDB and return a UserDetail which is then further transformed into UsernamePasswordAuthenticationToken
 * <p>
 * This function is also going to be using JWTService to validate and get the username
 */
@Component
@Log4j2
public class AuthManager implements ReactiveAuthenticationManager {
    final JWTService jwtService; //JWT token validation and getting username
    final MongoUSerDetailService mongoUserDetailService; //For Talking with DBService and returning a MongoUserDetail of the user we are trying to authenticate

    public AuthManager(JWTService jwtService, MongoUSerDetailService mongoUserDetailService) {
        this.jwtService = jwtService;
        this.mongoUserDetailService = mongoUserDetailService;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {

        return Mono.justOrEmpty(
                authentication
        )
                //Here begins the step of making changes to the mono object, firstly we cast it to BearerToken. Why? Because in AuthConverter we returned a BearerToken object
                .cast(BearerToken.class)
                //Next, we are going to do some processing with the authentication object.
                .flatMap(auth -> {
                    //Why did we use flatmap? Because if we don't the return type becomes Mono<Mono<Object>> instead of Mono<Authentication> as Map covers the return type with a Mono
                    String userName = jwtService.getUserID(auth.getCredentials()); //Get the username using JWT Service
                    Mono<UserDetails> user = mongoUserDetailService.findByUsername(userName).switchIfEmpty(Mono.error(new Exception("User not found"))); //Get MongoUserDetails by using our MongoUserDetailService
                    //Why do we use flatmap? Because If we map to transform an object we can't return Mono.error as this will change the type to Mono<Mono<Exception>> But we need Mono<Authentication>
                    return user.<Authentication>flatMap(u -> {
                        //Check if user is valid
                        if (u.getUsername() == null) {
                            log.info("User not found");
                            return Mono.error(new Exception("User not found"));
                        }
                        //validate token
                        if (jwtService.isValid(auth.getCredentials(), u.getUsername())) {
                            return Mono.just(new UsernamePasswordAuthenticationToken(u.getUsername(), u.getPassword(), u.getAuthorities()));
                        }
                        log.info("Invalid / Expired Token : {}", auth.getCredentials());
                        return Mono.error(new Exception("Invalid/Expired Token"));
                    });
                });
    }

}
