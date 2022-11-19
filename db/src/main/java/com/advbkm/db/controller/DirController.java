package com.advbkm.db.controller;

import com.advbkm.db.models.entities.EntityDir;
import com.advbkm.db.models.exception.ResponseException;
import com.advbkm.db.models.reqresp.ReqResp;
import com.advbkm.db.service.DirService;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
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
        log.info("Create dir for {} called ", userID);
        Mono<String> createdDir = dirService.createDir(dir, userID);

        return createdDir.map(
                entityDir -> ResponseEntity.status(200).body(new ReqResp<>(entityDir, "Success"))
        );
    }

    public @GetMapping("/{dirID}")
    Mono<ResponseEntity<ReqResp<EntityDir>>> getDir(@PathVariable String dirID, @RequestHeader("Authorization") String userID) {
        return dirService.getDir(dirID, userID)
                .map(entityDir -> ResponseEntity.ok(new ReqResp<>(entityDir, "")));
    }

    public @DeleteMapping("/{id}")
    Mono<ResponseEntity<ReqResp<Boolean>>> deleteDir(@PathVariable String id, @RequestHeader("Authorization") String userID) {
        log.info("Delete Dir endpoint Triggered with ID {} and user {}", id, userID);
        return dirService.deleteDir(id, userID)
                .map(b -> ResponseEntity.ok().body(new ReqResp<>(b, "Success")));
    }

    public @GetMapping("/getChildren/{parentID}")
    ResponseEntity<Flux<ReqResp<EntityDir>>> getDirs(@RequestHeader("Authorization") String userID, @PathVariable String parentID) {
        log.info("Get Dirs endpoint triggered for user {} and parentID {}", userID, parentID);
            var a = dirService.getChildrenDirs(userID, parentID)
                    .map(entityDir -> new ReqResp<>(entityDir, ""));
            return ResponseEntity.ok(a);

    }
}
