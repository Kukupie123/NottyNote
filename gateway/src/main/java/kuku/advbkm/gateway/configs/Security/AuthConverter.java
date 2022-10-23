package kuku.advbkm.gateway.configs.Security;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Security Note 3 :
 * Implements the ServerAuthenticationConverter
 * Has a function that is going to be responsible for extraction the token from Authorization header and return a new Authentication Object
 * Which is going to be BearerToken in our case (Go to that class if you want to know a little about it, but it should be self-explanatory),
 * it is a child of AbstractAuthenticationToken which is a child of Authentication and stores the JWT token as its credentials.
 * This object is that we are returning is then going to be passed to AuthenticationManager.
 *
 * Please go to AuthManager for the next Security Note.
 */
@Component
public class AuthConverter
        implements ServerAuthenticationConverter {
    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        //Return Mono of first Auth header BUT after filtering and mapping to Authorization object in the end.
        //We Use BearerToken class which extends from AbstractAuthenticationToken and is a child of Authentication
        return Mono.justOrEmpty(

                        exchange.getRequest()
                                .getHeaders()
                                .getFirst(HttpHeaders.AUTHORIZATION)
                )
                .filter(s -> s.startsWith("Bearer "))
                .map(s -> s.substring(7))
                .map(s -> new BearerToken(s));
    }
}
