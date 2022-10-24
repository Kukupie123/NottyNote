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
    private final JWTService jwtService;

    public DbDirService(JWTService jwtService) {
        this.jwtService = jwtService;
    }

    /**
     * Creates a new directory as the user
     *
     * @param dir      The Directory Payload, the createdBy will be set based on JWT token
     * @param jwtToken JWT token to extract the userID from and set as createdBy
     * @return ID of the newly created folder
     */
    public Mono<ResponseEntity<ReqResp<String>>> createDir(DirectoryModel dir, String jwtToken) {
        //Get userName from the JWT token
        String userID = jwtService.getUserName(jwtToken);
        dir.setCreatorID(userID);

        //prepare client to talk with db service
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
