package kuku.advbkm.gateway.service;


import kuku.advbkm.gateway.models.DirectoryModel;
import kuku.advbkm.gateway.models.ReqResp.ReqResp;
import kuku.advbkm.gateway.models.ResponseExceptionModel;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import util.urls.URLs;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
        String userID = jwtService.getUserID(jwtToken);

        //prepare client to talk with db service
        WebClient client = WebClient.create(
                URLs.DB_HOST(8000) + URLs.DIR_CREATE
        );


        return client.post().bodyValue(dir).header("Authorization", userID)
                .exchangeToMono(resp -> {

                    Mono<ReqResp> body = resp.bodyToMono(ReqResp.class);

                    return body.map(b -> {
                        String msg = b.getMsg();
                        String dirID = (String) b.getData();
                        return ResponseEntity.status(resp.rawStatusCode()).body(new ReqResp<>(dirID, msg));
                    });

                });

    }

    /**
     * Deletes a directory if they are the creator of that directory or if they are an admin
     *
     * @param dirID    the dirID that we are trying to delete
     * @param jwtToken jwt token to extract the userID from, it has to be the subject
     * @return response entity
     */
    public Mono<ResponseEntity<ReqResp<Boolean>>> deleteDir(String dirID, String jwtToken) {
        String url = URLs.DB_HOST(8000) + URLs.DIR_GET_DELETE(dirID);
        log.info("Delete Dir service function called on URL {}", url);
        String userID = jwtService.getUserID(jwtToken);
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

    public Mono<ResponseEntity<ReqResp<DirectoryModel>>> getDir(String dirID, String token) {
        String url = URLs.DB_HOST(8000) + URLs.DIR_DELETE_GET(dirID);
        String userID = jwtService.getUserID(token);

        WebClient client = WebClient.create(url);
        return client.get()
                .header("Authorization", userID)
                .exchangeToMono(resp -> {
                    var bodyMono = resp.bodyToMono(ReqResp.class);
                    return bodyMono.map(body -> {
                        log.info(body.getData().toString());
                        var castedData = mapToDir((Map<String, Object>) body.getData());
                        return ResponseEntity.status(resp.statusCode()).body(new ReqResp<>(castedData, body.getMsg()));
                    });
                });
    }

    public Flux<ReqResp<DirectoryModel>> getChildrenDirs(String parentID, String token) {
        String userID = jwtService.getUserID(token);
        String url = URLs.DB_HOST(8000) + URLs.DIR_GET_CHILDREN(parentID);

        WebClient client = WebClient.create(url);
        return client
                .get()
                .header("Authorization", userID)
                .exchangeToFlux(resp -> {
                    var body = resp.bodyToFlux(ReqResp.class);
                    return body.flatMap(reqResp -> {
                        if (resp.statusCode() != HttpStatus.OK) {
                            log.info("Status is not 200");
                            return Flux.error(new ResponseExceptionModel(reqResp.getMsg(), resp.rawStatusCode()));
                        }
                        LinkedHashMap data = (LinkedHashMap) reqResp.getData();
                        var castedData = new DirectoryModel((String) data.get("id"), (String) data.get("creatorID"), (String) data.get("isPublic"), (String) data.get("name"), (String) data.get("parent"), (List<String>) data.get("children"), (List<String>) data.get("bookmarks"));
                        return Flux.just(new ReqResp<>(castedData, reqResp.getMsg()));
                    });

                });
    }

    private DirectoryModel mapToDir(Map<String, Object> map) {
        String id = (String) map.get("id");
        String creatorID = (String) map.get("creatorID");
        String isPublic = (String) map.get("isPublic");
        String name = (String) map.get("name");
        String parent = (String) map.get("parent");
        List<String> children = (List<String>) map.get("children");
        List<String> bookmarks = (List<String>) map.get("bookmarks");
        return new DirectoryModel(id, creatorID, isPublic, name, parent, children, bookmarks);
    }
}
