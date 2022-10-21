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

    //Used for encoding passwords
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    //Users list temp, to be replaced with actual users from database
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


    //Replaces the default SecurityFilterChain, this is where we setup how the security is going to be configured
    //The arguments are passed by Spring's Dependency Injection

    /**
     * Security Note 1 : This bean is going to override the default security config. We set the rules and configs of the security here.
     * @param http
     * @param jwtAuthManager
     * @param jwtAuthConverter
     * @return
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



/*
How Spring security with JWT works
1. Firstly we need to have a webfluxsecurity class like this
It is going to have a bean which returns SecurityWebFilterChain which is going to override the default securityFilterChain we have

2. We are going to setup the rules and other stuff in this function
3. We are going to add a filter at authentication level

4. To be able to create a filter we need to create our own AuthenticationConverter

It is going to be responsible for extracting the token from the authorization header and returning a class that is a child of AbstractAuthenticationToken

5.The abstractAuthToken can be skipped if we create anonymous class on the go inside the functions

6.After creating the AuthConverter we are going to create the AuthenticationManager class that is going to be validating the details extracted from the authconverter
What we need to know is that the parameter is going to be of the same type as the one we returned in AuthConverter which is BearerToken in our case
Inside this class's function we are going to use JWT functions that we created to validate and getUserName

7. Now we can continue creating our filter and adding it to the configuration as done here.


 */