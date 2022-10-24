package kuku.advbkm.gateway.service;


import kuku.advbkm.gateway.models.DirectoryModel;
import kuku.advbkm.gateway.models.ReqResp.ReqResp;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import util.urls.URLs;

@Service
public class DbDirService {

    /**
     * Creates a new directory as the user
     * @return ID of the newly created folder
     */
    public Mono<ResponseEntity<ReqResp<String>>> createDir(DirectoryModel dir) {


        WebClient client = WebClient.create(
                URLs.DB_HOST(8000) + URLs.DIR_CREATE
        );


        Mono<ResponseEntity<ReqResp<String>>> transformedResponse = client.post().bodyValue(dir).exchangeToMono(resp -> {

            Mono<ReqResp> body = resp.bodyToMono(ReqResp.class);

            Mono<ResponseEntity<ReqResp<String>>> transformedBody = body.map(b -> {
                String msg = b.getMsg();
                String dirID = (String) b.getData();
                return ResponseEntity.status(resp.rawStatusCode()).body(new ReqResp<>(dirID, msg));
            });

            return transformedBody;


        });

        return transformedResponse;


    }
}
