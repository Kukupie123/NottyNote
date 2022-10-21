package kuku.advbkm.gateway.configs.Security;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

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
