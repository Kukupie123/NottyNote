package kuku.advbkm.gateway.controllers;


import kuku.advbkm.gateway.configs.Security.MongoUSerDetailService;
import kuku.advbkm.gateway.configs.Security.MongoUserDetails;
import kuku.advbkm.gateway.models.ReqResp.ReqResp;
import kuku.advbkm.gateway.models.UserModel;
import kuku.advbkm.gateway.service.DbUserService;
import kuku.advbkm.gateway.service.JWTService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.security.Principal;

@RestController()
@RequestMapping("/api/v1/gate/auth/")
public class AuthController {

    final PasswordEncoder passwordEncoder;
    final MongoUSerDetailService userService;
    final JWTService jwtService;
    final DbUserService dbUserService;

    public AuthController(PasswordEncoder passwordEncoder, MongoUSerDetailService userService, JWTService jwtService, DbUserService dbUserService) {
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
        this.jwtService = jwtService;
        this.dbUserService = dbUserService;
    }

    @PostMapping("/reg")
    public Mono<ResponseEntity<ReqResp<Boolean>>> register(@RequestBody MongoUserDetails user) {

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        //We are expecting email,name,password. The type is going to be set by db service
        return dbUserService.addUser(user);
    }


    @PostMapping("/login")
    public Mono<ResponseEntity<ReqResp<String>>> login(@RequestBody UserModel userLogin) {
        System.out.println("LOGIN ENDPOINT TRIGGERED");
        //find the user and if not found create an anonymous class
        Mono<UserDetails> user = userService.findByUsername(userLogin.getEmail()).switchIfEmpty(Mono.error(new Exception("User not registered")));

        //Transform UserDetail Mono to response entity as well as handle exception which might have occurred due to internal process
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

