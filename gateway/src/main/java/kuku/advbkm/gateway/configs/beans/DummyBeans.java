package kuku.advbkm.gateway.configs.beans;

import kuku.advbkm.gateway.configs.Security.MongoUserDetails;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Beans that are going to be dummy values.
 * Usually used to put objects in Mono when it returns Empty
 */
@Configuration
public class DummyBeans {
    @Bean(name = "dummy_MongoUserDetails")
    public MongoUserDetails dummyUserDetails() {
        return new MongoUserDetails(null,null,null,null);
    }
}
