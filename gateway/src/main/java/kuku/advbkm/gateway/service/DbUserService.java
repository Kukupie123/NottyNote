package kuku.advbkm.gateway.service;

import kuku.advbkm.gateway.configs.Security.MongoUserDetails;
import kuku.advbkm.gateway.models.ReqResp.ReqResp;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;

@Service
public class DbUserService {

    /**
     * Talks to db service and returns user if found.
     *
     * @return Type will be of ResponseEntity of MongoUserDetails. The caller's duty will be of handling
     */
    public Mono<ResponseEntity<ReqResp<MongoUserDetails>>> getUser(String id) {
        //Create a client and talk to db service
        WebClient client = WebClient.create("http://localhost:8000/api/v1/db/user/get/" + id);

        //Send get request and for the return value we are going to do exchangeToMono. This will give us access to the response.
        Mono<ResponseEntity<ReqResp<MongoUserDetails>>> transformedResponse = client.get().exchangeToMono(res -> {


            //Get the body of the response and cast it to MongoUserDetails type
            Mono<ReqResp> mud = res.bodyToMono(ReqResp.class);

            Mono<ResponseEntity<ReqResp<MongoUserDetails>>> transformedMud = mud.map(e -> {

                if (!res.statusCode().is2xxSuccessful()) {
                    return ResponseEntity.status(res.rawStatusCode()).body(new ReqResp<>(null, ""));
                }

                var mappedData = (LinkedHashMap) e.getData(); // The data is going to be set to linked hast map type, so we cast it.
                var castedData = new MongoUserDetails((String) mappedData.get("email"), (String) mappedData.get("password"), (String) mappedData.get("name"), (String) mappedData.get("type"));

                return ResponseEntity.status(res.rawStatusCode())
                        .body(new ReqResp<>(castedData, e.getMsg()));
            });

            return transformedMud;

        });

        return transformedResponse;

    }

    public Mono<ResponseEntity<ReqResp<Boolean>>> addUser(MongoUserDetails user) {
        WebClient client = WebClient.create("http://localhost:8000/api/v1/db/user/reg");
        var transformedResp = client.post().bodyValue(user)
                .exchangeToMono(resp -> {
                    Mono<ReqResp> body = resp.bodyToMono(ReqResp.class);

                    Mono<ResponseEntity<ReqResp<Boolean>>> transformedBody = body.map(b -> {
                        boolean castedData = (Boolean) b.getData();
                        return ResponseEntity.status(resp.rawStatusCode()).body(new ReqResp<>(castedData, b.getMsg()));
                    });

                    return transformedBody;
                });


        return transformedResp;

    }
}
