package kuku.advbkm.gateway.controllers;

import kuku.advbkm.gateway.configs.Security.MongoUSerDetailService;
import kuku.advbkm.gateway.configs.Security.MongoUserDetails;
import kuku.advbkm.gateway.models.ReqResp.ReqResp;
import kuku.advbkm.gateway.models.ResponseExceptionModel;
import kuku.advbkm.gateway.models.UserModel;
import kuku.advbkm.gateway.service.AuthService;
import kuku.advbkm.gateway.service.DbUserService;
import kuku.advbkm.gateway.service.JWTService;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Log4j2
@RestController()
@RequestMapping("/api/v1/gate/auth/")
public class AuthController {

    final PasswordEncoder passwordEncoder;
    final MongoUSerDetailService userService;
    final JWTService jwtService;
    final DbUserService dbUserService;
    final AuthService authService;

    public AuthController(PasswordEncoder passwordEncoder, MongoUSerDetailService userService, JWTService jwtService, DbUserService dbUserService, AuthService authService) {

        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
        this.jwtService = jwtService;
        this.dbUserService = dbUserService;
        this.authService = authService;
    }

    @PostMapping("/reg")
    public Mono<ResponseEntity<ReqResp<Boolean>>> register(@RequestBody MongoUserDetails user) {
        return authService.reg(user.getEmail(), user.getPassword(), user.getName())
                .onErrorResume(e -> {
                    var f = (ResponseExceptionModel) e;
                    return Mono.just(ResponseEntity.status(f.getStatusCode()).body(new ReqResp<>(null, f.getMessage())));
                });
    }


    @PostMapping("/login")
    public Mono<ResponseEntity<ReqResp<String>>> login(@RequestBody UserModel userLogin) {
        log.info("Logging endpoint {}", userLogin.toString());
        return authService.login(userLogin.getEmail(), userLogin.getPassword())
                .onErrorResume(throwable -> {
                    var throwableCasted = (ResponseExceptionModel) throwable;
                    return Mono.just(ResponseEntity.status(throwableCasted.getStatusCode()).body(new ReqResp<>(null, throwableCasted.getMessage())));
                });
    }

    public @GetMapping("/validate")
    Mono<ResponseEntity<Boolean>> val() {
        log.info("Validation endpoint hit");
        return Mono.just(ResponseEntity.ok(true));
    }


}

