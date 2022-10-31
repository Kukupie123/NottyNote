package kuku.advbkm.gateway.controllers;


import kuku.advbkm.gateway.models.ReqResp.ReqResp;
import kuku.advbkm.gateway.models.TemplateModel;
import kuku.advbkm.gateway.service.JWTService;
import kuku.advbkm.gateway.service.TemplateService;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;


@Log4j2
@RestController
@RequestMapping("/api/v1/gate/temp/")
public class TempController {

    private final JWTService jwtService;
    private final TemplateService templateService;

    public TempController(JWTService jwtService, TemplateService templateService) {
        this.jwtService = jwtService;
        this.templateService = templateService;
    }

    @PostMapping("/create")
    public Mono<ResponseEntity<ReqResp<String>>> createTemplate(@RequestBody TemplateModel template, @RequestHeader("Authorization") String auth) {
        log.info("Create Template with struct : {}", template);
        String jwtToken = auth.substring(7);
        String userName = jwtService.getUserID(jwtToken);
        return templateService.createTemplate(template, userName)
                .onErrorResume(throwable -> Mono.just(ResponseEntity.status(500).body(new ReqResp<>(null, throwable.getMessage()))));

    }

    public @DeleteMapping("/{id}")
    Mono<ResponseEntity<ReqResp<Boolean>>> deleteTemp(@PathVariable String id, @RequestHeader("Authorization") String auth) {
        String jwtToken = auth.substring(7);
        String userName = jwtService.getUserID(jwtToken);
        return templateService.deleteTemplate(id, userName)
                .onErrorResume(throwable -> Mono.just(ResponseEntity.status(500).body(new ReqResp<>(null, throwable.getMessage()))));

    }
}
