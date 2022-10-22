package kuku.advbkm.gateway.configs.Security;

import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;

/*
Link to material used as reference
https://www.youtube.com/watch?v=wyl06YqMxaU&t=161s
 */
@EnableWebFluxSecurity
public class SecurityConfig {

    //We can add @Bean annotations because EnableWebFluxSecurity has @Configuration annotation already

    //Used for encoding passwords
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    //Our custom implementation of getting records from our database for authentication
    @Bean
    MongoUSerDetailService userDetailService() {
        //Used by AuthManager class to get user from db service
        return new MongoUSerDetailService();
    }


    //Replaces the default SecurityFilterChain, this is where we set up how the security is going to be configured
    //The arguments are passed by Spring's Dependency Injection

    /**
     * Security Note 1 : This bean is going to override the default security config. We set the rules and configs of the security here.
     */
    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http, AuthManager jwtAuthManager, AuthConverter jwtAuthConverter) {
        //NOTE : The parameters are injected by spring as they have been marked as component/Bean class

        /**
         * Security Note 2 : Creating a new filter at Authentication layer and using our own AuthenticationConverter and AuthenticationManager.
         * Please Check AuthConverter for the next note.
         */
        AuthenticationWebFilter jwtFilter = new AuthenticationWebFilter(jwtAuthManager);
        jwtFilter.setServerAuthenticationConverter(jwtAuthConverter);
        return http
                .authorizeExchange(auth -> { //Setting up authorization of uri
                    auth.pathMatchers(HttpMethod.POST, "/api/v1/gate/auth/login").permitAll(); //Everyone is allowed to enter these two uri
                    auth.pathMatchers(HttpMethod.POST, "/api/v1/gate/auth/reg").permitAll();
                    auth.anyExchange().authenticated(); //Rest of the endpoint needs to be authenticated
                })
                .addFilterAt(jwtFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .httpBasic().disable() // disable httpBasic as we are going to use JWT
                .formLogin().disable() //disable form login
                .csrf().disable() //disable csrf, I don't know what this is for. People say to enable this when using web app
                .build();


    }
}



/*
How Spring security with JWT works
Things to know.
Our MongoDB is where users record are stored. It is handled by db service so we need to talk to it to get users list

Class we need to create
MongoUserDetails bean will implement userDetails and override the function to return the correct data

Beans
MongoUserDetailsService will implement ReactiveUserDetailsService and override function that is going to return a UserDetail.
This is where we need to talk to backend to get userinfo from db service and convert it into MongoUserDetails and return it

Service : DBService is going to handle talking to db service, parsing response and returning back MongoUserDetails


1. We need to create SecurityFilter and create a filter along with other configs
2. Create custom AuthConverter class that will extract JWT token from auth header
3. Create AuthManager that is going to make use to authConverter to get userName.
We then use this on our bean MongoUserDetailsService and get back a MongoUserDetail.

We can then validate the token as well as the userDetail



 */