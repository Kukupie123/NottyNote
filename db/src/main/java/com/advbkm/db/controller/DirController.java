package com.advbkm.db.controller;

import com.advbkm.db.models.entities.EntityDir;
import com.advbkm.db.models.exception.ResponseException;
import com.advbkm.db.models.reqresp.ReqResp;
import com.advbkm.db.service.DirService;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Log4j2
@RestController
@RequestMapping("/api/v1/db/dir")
public class DirController {

    private final DirService dirService;

    public DirController(DirService dirService) {
        this.dirService = dirService;
    }

    public @PostMapping("/create")
    Mono<ResponseEntity<ReqResp<String>>> createDir(@RequestBody EntityDir dir, @RequestHeader("Authorization") String userID) {
        System.out.println("CREATE DIR CALLED " + dir);
        Mono<String> createdDir = dirService.createDir(dir, userID);

        return createdDir.map(
                entityDir -> ResponseEntity.status(200).body(new ReqResp<>(entityDir, "Success"))
        ).onErrorResume(throwable -> Mono.just(
                ResponseEntity
                        .status(((ResponseException) throwable).getStatusCode())
                        .body(new ReqResp<>(null, throwable.getMessage()))

        ));
    }

    public @DeleteMapping("/{id}")
    Mono<ResponseEntity<ReqResp<Boolean>>> deleteDir(@PathVariable String id, @RequestHeader("Authorization") String userID) {
        log.info("Delete Dir endpoint Triggered with ID {} and user {}", id, userID);
        return dirService.deleteDir(id, userID)
                .map(b -> ResponseEntity.ok().body(new ReqResp<>(b, "Success")))
                .onErrorResume(
                        throwable -> {
                            log.info("Exception in delete endpoint {}", throwable.getMessage());
                            return Mono.just(
                                    ResponseEntity
                                            .status(((ResponseException) throwable).getStatusCode())
                                            .body(new ReqResp<>(false, throwable.getMessage()))
                            );
                        }
                );
    }
}
