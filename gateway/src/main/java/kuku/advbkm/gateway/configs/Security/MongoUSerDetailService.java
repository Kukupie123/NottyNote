package kuku.advbkm.gateway.configs.Security;

import kuku.advbkm.gateway.service.DbUserService;
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
    private DbUserService dbUserService;


    @Override
    public Mono<UserDetails> findByUsername(String username) {
        //getUser() returns a Mono<ResponseEntity<ReqResp<MongoUserDetails>>>
        //We need to transform it to Mono<UserDetails>
        return dbUserService.getUser(username)
                .mapNotNull(resp -> {
                    //If status code is success
                    if (resp.getStatusCode().is2xxSuccessful()) {
                        return Objects.requireNonNull(resp.getBody()).getData();
                    }
                    return null;
                });
    }
}
