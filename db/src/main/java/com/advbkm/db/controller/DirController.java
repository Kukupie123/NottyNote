package com.advbkm.db.controller;

import com.advbkm.db.models.reqresp.ReqResp;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/db/dir")
public class DirController {
    public @PostMapping("/create")
    Mono<ResponseEntity<ReqResp<String>>> createDir() {

        return null;
    }
}
