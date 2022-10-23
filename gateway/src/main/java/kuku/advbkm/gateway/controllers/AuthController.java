package kuku.advbkm.gateway.controllers;


import kuku.advbkm.gateway.configs.Security.MongoUSerDetailService;
import kuku.advbkm.gateway.configs.Security.MongoUserDetails;
import kuku.advbkm.gateway.models.ReqRespBodies.RequestUserLogin;
import kuku.advbkm.gateway.models.ReqRespModel.ReqResp;
import kuku.advbkm.gateway.service.DbService;
import kuku.advbkm.gateway.service.JWTService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.security.Principal;

@SuppressWarnings("ALL")
@RestController()
@RequestMapping("/api/v1/gate/auth/")
public class AuthController {

    final PasswordEncoder passwordEncoder;
    final MongoUSerDetailService userService;
    final JWTService jwtService;
    final DbService dbService;
    @Qualifier("dummy_MongoUserDetails") //Makes sure that we get the correct bean
    final UserDetails dummyUserDetail; //Dummy UserDetails Bean that we created in configs.beans.DummyBeans to use in functions below

    public AuthController(PasswordEncoder passwordEncoder, MongoUSerDetailService userService, JWTService jwtService, DbService dbService, UserDetails dummyUserDetail) {
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
        this.jwtService = jwtService;
        this.dbService = dbService;
        this.dummyUserDetail = dummyUserDetail;
    }

    @PostMapping("/reg")
    public Mono<ResponseEntity<ReqResp<Boolean>>> register(@RequestBody MongoUserDetails user) {
        System.out.println(String.format("Registering : %s", user));

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        //We are expecting email,name,password. The type is going to be set by db service
        return dbService.addUser(user);
    }


    @PostMapping("/login")
    public Mono<ResponseEntity<ReqResp<String>>> login(@RequestBody RequestUserLogin userLogin) {
        //find the user and if not found create an anonymous class
        Mono<UserDetails> user = userService.findByUsername(userLogin.getEmail()).defaultIfEmpty(dummyUserDetail);

        //Transform UserDetail Mono to response entity as well as handle exception which might have occoured due to internal process
        var userMap = user.map(u -> {
            //Check if user was found
            if (u.getUsername() == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ReqResp<>("User not registered", ""));
            }
            //Check if password and username matches
            if (u.getUsername().equals(userLogin.getEmail()) && passwordEncoder.matches(userLogin.getPassword(), u.getPassword())) {
                return ResponseEntity.ok(new ReqResp<>(jwtService.generate(u.getUsername()), "Success"));
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ReqResp<>("Wrong Credentials", ""));
        });


        return userMap;

    }

    @GetMapping("/user")
    public Mono<ResponseEntity<ReqResp<String>>> user(@AuthenticationPrincipal Principal p) {
        return Mono.just(ResponseEntity.ok(new ReqResp<>("", p.getName())));

    }

}

