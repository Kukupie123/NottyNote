package com.advbkm.db.controller;


import com.advbkm.db.models.entities.EntityUser;
import com.advbkm.db.models.reqresp.ReqResp;
import com.advbkm.db.service.UserService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/db/user")
public class UserController {


    @Qualifier("dummy_EntityUser")

    final private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }


    @PostMapping("/reg")
    public Mono<ResponseEntity<ReqResp<Boolean>>> register(@RequestBody EntityUser user) {
        user.setType("USER");
        System.out.println("Registering user :" + user);
        //IMPORTANT : When you try to save EntityUser without a converter the ID(email) is converted into ObjectID instead of being saved as a string
        Mono<EntityUser> monoUser = userService.createUser(user);

        return monoUser.map(u -> {
            System.out.println(u);

            if (u.getEmail() == null) {
                //Invalid object
                return ResponseEntity.internalServerError().body(new ReqResp<>(false, "Something went wrong"));
            }
            return ResponseEntity.ok(new ReqResp<>(true, "Success"));
        });
    }

    @GetMapping("/get/{id}")
    public Mono<ResponseEntity<ReqResp<EntityUser>>> getUser(@PathVariable String id) {
        Mono<EntityUser> monoUser = userService.getUser(id);

        return monoUser.map(entityUser -> {
            if (entityUser.getEmail() == null) {
                return ResponseEntity.status(404).body(new ReqResp<>(null, "No user found"));
            }
            return ResponseEntity.ok(new ReqResp<>(entityUser, "Success"));
        });
    }


}
