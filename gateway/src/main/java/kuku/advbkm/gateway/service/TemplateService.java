package kuku.advbkm.gateway.service;


import kuku.advbkm.gateway.models.ReqResp.ReqResp;
import kuku.advbkm.gateway.models.TemplateField;
import kuku.advbkm.gateway.models.TemplateModel;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import util.urls.URLs;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

@Log4j2
@Service
public class TemplateService {

    private final JWTService jwtService;

    public TemplateService(JWTService jwtService) {
        this.jwtService = jwtService;
    }

    public Mono<ResponseEntity<ReqResp<String>>> createTemplate(TemplateModel templateModel, String token) {
        String userName = jwtService.getUserID(token);
        WebClient client = WebClient.create(URLs.DB_HOST(8000) + URLs.TEMP_CREATE);
        return client.post().header("Authorization", userName).bodyValue(templateModel)
                .exchangeToMono(resp -> resp.bodyToMono(ReqResp.class)
                        .map(body -> {
                            String templateID = (String) body.getData();
                            String msg = body.getMsg();
                            return ResponseEntity.status(resp.rawStatusCode()).body(new ReqResp<>(templateID, msg));

                        })
                );

    }

    public Mono<ResponseEntity<ReqResp<Boolean>>> deleteTemplate(String templateID, String token) {
        String userName = jwtService.getUserID(token);
        WebClient client = WebClient.create(URLs.DB_HOST(8000) + URLs.TEMP_DELETE_GET(templateID));
        return client.delete().header("Authorization", userName)
                .exchangeToMono(resp -> resp.bodyToMono(ReqResp.class)
                        .map(body -> {
                            boolean data = (boolean) body.getData();
                            String msg = body.getMsg();
                            return ResponseEntity.status(resp.rawStatusCode()).body(new ReqResp<>(data, msg));
                        }));

    }

    public Mono<ResponseEntity<ReqResp<TemplateModel>>> getTemplate(String templateID, String token) {
        String userName = jwtService.getUserID(token);
        WebClient client = WebClient.create(URLs.DB_HOST(8000) + URLs.TEMP_DELETE_GET(templateID));
        return client.get().header("Authorization", userName)
                .exchangeToMono(resp -> {
                    var bodyMono = resp.bodyToMono(ReqResp.class);
                    return bodyMono.map(body -> {
                        LinkedHashMap<String, Object> data = (LinkedHashMap<String, Object>) body.getData();
                        var template = new TemplateModel((String) data.get("id"), (String) data.get("name"), (String) data.get("creatorID"), (List<String>) data.get("bookmarks"), (HashMap<String, TemplateField>) data.get("struct"));
                        String msg = body.getMsg();
                        return ResponseEntity.status(resp.rawStatusCode()).body(new ReqResp<>(template, msg));
                    });
                });
    }
}