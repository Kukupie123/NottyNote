package kuku.advbkm.gateway.service;


import kuku.advbkm.gateway.models.ReqResp.ReqResp;
import kuku.advbkm.gateway.models.TemplateModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
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
                            var betterReqResp = new ReqResp<>(templateID, msg);
                            return new ResponseAndMonoBody(resp, betterReqResp);

                        })
                )
                //Now we will pack them into new responseEntity and return
                .map(
                        responseAndMonoBody -> ResponseEntity.status(responseAndMonoBody.getResponse().rawStatusCode())
                                .body(responseAndMonoBody.getBody())
                );

    }
}

@AllArgsConstructor
@Getter
//used by this service to store response and it's body in a single class
class ResponseAndMonoBody<T> {
    private final ClientResponse response;
    private final ReqResp<T> body;


}