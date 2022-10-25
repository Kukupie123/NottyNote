package com.advbkm.db.controller;

import com.advbkm.db.models.entities.EntityDir;
import com.advbkm.db.models.reqresp.ReqResp;
import com.advbkm.db.service.DirService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/db/dir")
public class DirController {

    private final DirService dirService;

    public DirController(DirService dirService) {
        this.dirService = dirService;
    }

    public @PostMapping("/create")
    Mono<ResponseEntity<ReqResp<String>>> createDir(@RequestBody EntityDir dir) {
        System.out.println("CREATE DIR CALLED " + dir);
        Mono<EntityDir> createdDir = dirService.createDir(dir);

        return createdDir.map(
                entityDir -> ResponseEntity.status(200).body(new ReqResp<>(entityDir.get_id(), "Success"))
        );
    }

    public @DeleteMapping("/{id}")
    Mono<ResponseEntity<ReqResp<Boolean>>> deleteDir(@PathVariable String id) {
        return dirService.deleteDir(id)
                .map(
                        success -> {
                            int status = 200;
                            if (success) {
                                status = 500;
                            }
                            return ResponseEntity.status(status).body(new ReqResp<>(success, ""));
                        }
                );
    }
}
