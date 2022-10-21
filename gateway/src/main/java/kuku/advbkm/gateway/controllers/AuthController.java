package kuku.advbkm.gateway.controllers;


import kuku.advbkm.gateway.models.ReqRespBodies.RequestUserLogin;
import kuku.advbkm.gateway.models.ReqRespModel.ReqRespModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController()
public class AuthController {

    final PasswordEncoder passwordEncoder;
    final ReactiveUserDetailsService userService;

    public AuthController(PasswordEncoder passwordEncoder, ReactiveUserDetailsService userService) {
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
    }

    @PostMapping("/reg")
    public Mono<ResponseEntity<ReqRespModel<Boolean>>> register() {
        return Mono.just(
                ResponseEntity.ok(new ReqRespModel<>("Successful", true))
        );
    }


    @PostMapping("/login")
    public Mono<ResponseEntity<ReqRespModel<String>>> login(@RequestBody RequestUserLogin userLogin) {
        //find the user and set the default to null if not found
        Mono<UserDetails> user = userService.findByUsername(userLogin.getEmail()).defaultIfEmpty(null);

        return user.flatMap(userDetails -> {
            //Check if user was found
            if (userDetails != null) {
                //Since user was found check if the password was correct
                if (passwordEncoder.matches(userLogin.getPassword(), userDetails.getPassword())) {
                    return Mono.just(
                            ResponseEntity.ok(new ReqRespModel<>("Successful", "JWT TOKEN"))
                    );
                }
            }


            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ReqRespModel<>("Invalid credentials", null)));


        });

    }
}

