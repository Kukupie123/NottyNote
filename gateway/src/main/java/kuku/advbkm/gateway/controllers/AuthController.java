package kuku.advbkm.gateway.controllers;


import kuku.advbkm.gateway.models.ReqRespBodies.RequestUserLogin;
import kuku.advbkm.gateway.models.ReqRespModel.ReqRespModel;
import kuku.advbkm.gateway.service.JWTService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.security.Principal;

@SuppressWarnings("ALL")
@RestController()
public class AuthController {

    final PasswordEncoder passwordEncoder;
    final ReactiveUserDetailsService userService;
    final JWTService jwtService;
    @Qualifier("dummy_userDetails") //Makes sure that we get the correct bean
    final UserDetails dummyUserDetail; //Dummy UserDetails Bean that we created in configs.beans.DummyBeans to use in functions below

    public AuthController(PasswordEncoder passwordEncoder, ReactiveUserDetailsService userService, JWTService jwtService, UserDetails dummyUserDetail) {
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
        this.jwtService = jwtService;
        this.dummyUserDetail = dummyUserDetail;
    }

    @PostMapping("/reg")
    public Mono<ResponseEntity<ReqRespModel<Boolean>>> register() {
        return Mono.justOrEmpty(
                ResponseEntity.ok(new ReqRespModel<>("Successful", true))
        );
    }


    @PostMapping("/login")
    public Mono<ResponseEntity<ReqRespModel<String>>> login(@RequestBody RequestUserLogin userLogin) {
        //find the user and if not found create an anonymous class
        Mono<UserDetails> user = userService.findByUsername(userLogin.getEmail()).defaultIfEmpty(dummyUserDetail);

        //Transform UserDetail Mono to response entity as well as handle exception which might have occoured due to internal process
        var userMap = user.map(u -> {
                    //Check if user was found
                    if (u.getUsername() == null) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ReqRespModel<>("User not registered", ""));
                    }
                    //Check if password and username matches
                    if (u.getUsername().equals(userLogin.getEmail()) && passwordEncoder.matches(userLogin.getPassword(), u.getPassword())) {
                        return ResponseEntity.ok(new ReqRespModel<>(jwtService.generate(u.getUsername()), "Success"));
                    }

                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ReqRespModel<>("Wrong Credentials", ""));
                })
                .onErrorResume(e -> {
                    return Mono.just(ResponseEntity.internalServerError().body(new ReqRespModel<>(e.getMessage(), "")));
                });

        return userMap;

    }

    @GetMapping("/user")
    public Mono<ResponseEntity<ReqRespModel<String>>> user(@AuthenticationPrincipal Principal p) {
        return Mono.just(
                ResponseEntity.ok(
                        new ReqRespModel<>("", p.getName())
                )
        );

    }

}

