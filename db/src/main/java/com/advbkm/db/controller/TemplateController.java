package com.advbkm.db.controller;


import com.advbkm.db.models.reqresp.ReqResp;
import com.advbkm.db.service.TemplateService;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Log4j2
@RestController()
@RequestMapping("/api/v1/db/template")
public class TemplateController {

    private final TemplateService templateService;

    public TemplateController(TemplateService templateService) {
        this.templateService = templateService;
    }

    public @PostMapping("/create")
    Mono<ResponseEntity<ReqResp<String>>> createTemplate() {
        return templateService.createTemplate()
                .map(s -> ResponseEntity.ok().body(new ReqResp<>(s, "Success")));
    }
}
