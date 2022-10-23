package com.advbkm.db.controller;


import com.advbkm.db.models.RequestModels.ReqRegisterUser;
import com.advbkm.db.models.entities.EntityUser;
import com.advbkm.db.models.reqresp.ReqResp;
import com.advbkm.db.repo.RepoUsers;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/db/user")
public class UserController {

    final private RepoUsers userRepo;

    @Qualifier("dummy_EntityUser")
    final private EntityUser dummyBean;

    public UserController(RepoUsers userRepo, EntityUser dummyBean) {
        this.userRepo = userRepo;
        this.dummyBean = dummyBean;
    }


    @PostMapping("/reg")
    public Mono<ResponseEntity<ReqResp<Boolean>>> register(@RequestBody ReqRegisterUser user) {
        Mono<EntityUser> monoUser = userRepo.save(new EntityUser(user.getEmail(), user.getPassword(), user.getName(), "user"))
                .onErrorReturn(dummyBean) //if we face an error we want to return a dummy EntityUser obj
                .defaultIfEmpty(dummyBean); //Same as above

        return monoUser.map(u -> {
            if (u.getEmail() == null) {
                //Invalid object
                return ResponseEntity.internalServerError().body(new ReqResp<>(false, "Something went wrong"));
            }
            return ResponseEntity.ok(new ReqResp<>(true, "Success"));
        });
    }

    @GetMapping("/get/{id}")
    public Mono<ResponseEntity<ReqResp<EntityUser>>> getUser(@PathVariable String id) {
        System.out.println("Get called");
        Mono<EntityUser> monoUser = userRepo.findById(id).defaultIfEmpty(dummyBean).onErrorReturn(dummyBean);

        return monoUser.map(entityUser -> {
            if (entityUser.getEmail() == null) {
                System.out.println("No user found with id " + id);
                return ResponseEntity.status(404).body(new ReqResp<>(null, "No user found"));
            }
            System.out.println(" user found with id " + id);
            return ResponseEntity.ok(new ReqResp<>(entityUser, "Success"));
        });
    }


}
