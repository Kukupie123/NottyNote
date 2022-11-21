package kuku.advbkm.gateway.controllers;


import kuku.advbkm.gateway.models.ReqResp.ReqResp;
import kuku.advbkm.gateway.models.TemplateModel;
import kuku.advbkm.gateway.service.TemplateService;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@CrossOrigin(value = "**") //Allow all origin, all headers, All Http methods.
@Log4j2
@RestController
@RequestMapping("/api/v1/gate/temp/")
public class TempController {

    private final TemplateService templateService;

    public TempController(TemplateService templateService) {
        this.templateService = templateService;
    }

    @PostMapping("/create")
    public Mono<ResponseEntity<ReqResp<String>>> createTemplate(@RequestBody TemplateModel template, @RequestHeader("Authorization") String auth) {
        log.info("Create Template with struct : {}", template);
        String jwtToken = auth.substring(7);
        return templateService.createTemplate(template, jwtToken)
                ;
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<ReqResp<TemplateModel>>> getTemplate(@PathVariable String id, @RequestHeader("Authorization") String auth) {
        log.info("Get Template with id {}", id);
        String jwtToken = auth.substring(7);
        return templateService.getTemplate(id, jwtToken);
    }

    public @DeleteMapping("/{id}")
    Mono<ResponseEntity<ReqResp<Boolean>>> deleteTemp(@PathVariable String id, @RequestHeader("Authorization") String auth) {
        String jwtToken = auth.substring(7);
        return templateService.deleteTemplate(id, jwtToken)
                ;
    }
}
