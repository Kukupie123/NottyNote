package kuku.advbkm.gateway.service;

import kuku.advbkm.gateway.configs.Security.MongoUSerDetailService;
import kuku.advbkm.gateway.configs.Security.MongoUserDetails;
import kuku.advbkm.gateway.models.ReqResp.ReqResp;
import kuku.advbkm.gateway.models.ResponseExceptionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class AuthService {
    final MongoUSerDetailService userService;
    final JWTService jwtService;
    final PasswordEncoder passwordEncoder;
    final DbUserService dbUserService;

    public AuthService(MongoUSerDetailService userService, JWTService jwtService, PasswordEncoder passwordEncoder, DbUserService dbUserService) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.dbUserService = dbUserService;
    }

    public Mono<ResponseEntity<ReqResp<String>>> login(String userID, String password) {
        Mono<UserDetails> user = userService.findByUsername(userID.toLowerCase()).switchIfEmpty(Mono.error(new ResponseExceptionModel("User not registered", 404)));

        return user.map(u -> {
            //Check if password and username matches
            if (u.getUsername().equals(userID) && passwordEncoder.matches(password, u.getPassword())) {
                return ResponseEntity.ok(new ReqResp<>(jwtService.generate(u.getUsername()), "Success"));
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ReqResp<>("Wrong Credentials", "Wrong Credentials"));
        });

    }

    public Mono<ResponseEntity<ReqResp<Boolean>>> reg(String userID, String password, String name) {
        if (userID.isEmpty() || password.isEmpty() || name.isEmpty())
            return Mono.error(new ResponseExceptionModel("Invalid Field", 401));
        MongoUserDetails user = new MongoUserDetails(userID.toLowerCase(), "", name, "USER");
        user.setPassword(passwordEncoder.encode(password));
        return dbUserService.addUser(user)
                .thenReturn(ResponseEntity.ok().body(new ReqResp<>(true, ""))
                )
                .onErrorResume(e -> Mono.error(new ResponseExceptionModel(e.getMessage(), 500)));
    }
}
