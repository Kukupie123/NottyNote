package kuku.advbkm.gateway.configs.Security;

import kuku.advbkm.gateway.service.DbService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * Used inside AuthManager's authenticate function to get userDetails from db service
 */
public class MongoUSerDetailService implements ReactiveUserDetailsService {
    @Autowired
    private DbService dbService;


    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return dbService.getUser(username)
                .mapNotNull(resp -> {
                    if (resp.getStatusCode().is2xxSuccessful()) {
                        return Objects.requireNonNull(resp.getBody()).getData();
                    }
                    return null;
                });
    }
}
