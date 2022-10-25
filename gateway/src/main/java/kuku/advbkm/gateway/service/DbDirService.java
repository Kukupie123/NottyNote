package kuku.advbkm.gateway.service;


import kuku.advbkm.gateway.models.DirectoryModel;
import kuku.advbkm.gateway.models.ReqResp.ReqResp;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import util.urls.URLs;

@Log4j2
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

    /**
     * Deletes a directory if they are the creator of that directory or if they are an admin
     *
     * @param dirID    the dirID that we are trying to delete
     * @param jwtToken jwt token to extract the userID from, it has to be the subject
     * @return response entity
     */
    public Mono<ResponseEntity<ReqResp<Boolean>>> deleteDir(String dirID, String jwtToken) {
        String url = URLs.DB_HOST(8000) + URLs.DIR_DELETE(dirID);
        log.info("Delete Dir service function called on URL {}", url);
        String userID = jwtService.getUserName(jwtToken);
        WebClient client = WebClient.create(url);

        return client
                .delete()
                .header("Authorization", userID)
                .exchangeToMono(resp -> {
                    log.info(resp.statusCode());
                    var bodyMono = resp.bodyToMono(ReqResp.class);

                    return bodyMono.flatMap(body -> {
                        log.info(body.getData());
                        boolean created = (Boolean) body.getData();
                        return Mono.just(
                                ResponseEntity.status(resp.rawStatusCode()).body(new ReqResp<>(created, body.getMsg()))
                        );
                    });

                });
    }
}
