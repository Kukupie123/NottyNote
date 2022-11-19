package kuku.advbkm.gateway.service;


import kuku.advbkm.gateway.models.ReqResp.ReqResp;
import kuku.advbkm.gateway.models.TemplateModel;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import util.urls.URLs;

@Service
public class TemplateService {

    public Mono<ResponseEntity<ReqResp<String>>> createTemplate(TemplateModel templateModel, String userID) {
        WebClient client = WebClient.create(URLs.DB_HOST(8000) + URLs.TEMP_CREATE);

        return client.post().header("Authorization", userID).bodyValue(templateModel)
                .exchangeToMono(resp -> resp.bodyToMono(ReqResp.class)
                        .map(body -> {
                            String templateID = (String) body.getData();
                            String msg = body.getMsg();
                            return ResponseEntity.status(resp.rawStatusCode()).body(new ReqResp<>(templateID, msg));

                        })
                );

    }

    public Mono<ResponseEntity<ReqResp<Boolean>>> deleteTemplate(String templateID, String userID) {
        WebClient client = WebClient.create(URLs.DB_HOST(8000) + URLs.TEMP_DELETE_GET(templateID));
        return client.delete().header("Authorization", userID)
                .exchangeToMono(resp -> resp.bodyToMono(ReqResp.class)
                         .map(body -> {
                             boolean data = (boolean) body.getData();
                             String msg = body.getMsg();
                             return ResponseEntity.status(resp.rawStatusCode()).body(new ReqResp<>(data, msg));
                         }));

    }
}