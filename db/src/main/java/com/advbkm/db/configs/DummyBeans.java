package com.advbkm.db.configs;


import com.advbkm.db.models.entities.EntityDir;
import com.advbkm.db.models.entities.EntityUser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DummyBeans {


    /**
     * Returns a dummy EntityUser object which will be used when passing Mono<EntityUsr> but we face an error or get empty result
     */
    @Bean("dummy_EntityUser")
    public EntityUser dummyUser() {
        return new EntityUser(null, null, null, null);
    }

    public @Bean
    EntityDir dummyDir() {
        return new EntityDir(null, null, null, null, null, null, null);
    }
}
