package com.kuku.advanced.bookmark.controller;

import models.reqRespBodies.ReqBodyRegister;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/public")
public class MainController {

    @RequestMapping("/reg")
    private Mono<String> register(@RequestBody ReqBodyRegister body) {
        // Security checks? FUTURE
        // Call user service
        // Return the response of user service
        return Mono.just("WIP");
    }
}
