package kuku.advbkm.gateway.configs.Security;

import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;

/*
Link to material used as reference
https://www.youtube.com/watch?v=wyl06YqMxaU&t=161s
 */
@EnableWebFluxSecurity
public class SecurityConfig {

    //We can add @Bean annotations because EnableWebFluxSecurity has @Configuration annotation already


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    //Users list temp
    @Bean
    public MapReactiveUserDetailsService userDetailsService(PasswordEncoder encoder) {
        UserDetails user = User.builder() //Build user
                .username("kuku")
                .password(encoder.encode("kuku"))
                .roles("USER")
                .build();

        //Create new obj and pass the user as arg
        return new MapReactiveUserDetailsService(user);

    }

    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http, AuthManager jwtAuthManager, AuthConverter jwtAuthConverter) {
        AuthenticationWebFilter jwtFilter = new AuthenticationWebFilter(jwtAuthManager);
        jwtFilter.setServerAuthenticationConverter(jwtAuthConverter);
        return http
                .authorizeExchange(auth -> { //Setting up authorization of uri
                    auth.pathMatchers(HttpMethod.POST, "/login").permitAll(); //Everyone is allowed to enter these two uri
                    auth.pathMatchers(HttpMethod.POST, "/reg").permitAll();
                    auth.anyExchange().authenticated(); //Rest of the endpoint needs to be authenticated
                })
                .addFilterAt(jwtFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .httpBasic().disable() // disable httpBasic as we are going to use JWT
                .formLogin().disable() //disable form login
                .csrf().disable() //disable csrf, I don't know what this is for. People say to enable this when using web app
                .build();


    }
}


//TODO: Add Explanation on how JWT and Security was setup