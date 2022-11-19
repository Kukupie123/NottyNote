package kuku.advbkm.gateway.service;


import kuku.advbkm.gateway.models.BookmarkModel;
import kuku.advbkm.gateway.models.ReqResp.ReqResp;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import util.urls.URLs;

@Service
@Log4j2
public class DbBookmarkService {

    private final JWTService jwtService;

    public DbBookmarkService(JWTService jwtService) {
        this.jwtService = jwtService;
    }

    public Mono<ResponseEntity<ReqResp<String>>> createBookmark(BookmarkModel bookmarkModel, String jwtToken) {

        String userID = jwtService.getUserID(jwtToken);

        WebClient client = WebClient.create(URLs.DB_HOST(8000) + URLs.BKM_CREATE);

        return client.post().header("Authorization", userID).bodyValue(bookmarkModel)
                //Get the body
                .exchangeToMono(resp -> resp.bodyToMono(ReqResp.class)
                        .map(body -> {
                            String id = (String) body.getData();
                            String msg = body.getMsg();
                            return ResponseEntity.status(resp.rawStatusCode()).body(new ReqResp<>(id, msg));
                        }));

    }

    public Mono<ResponseEntity<ReqResp<Boolean>>> deleteBookmark(String id, String jwtToken) {

        String userID = jwtService.getUserID(jwtToken);
        log.info("UserID extracted is {}", userID);


        WebClient client = WebClient.create(URLs.DB_HOST(8000) + URLs.BKM_DELETE_GET(id));

        return client.delete().header("Authorization", userID)
                .exchangeToMono(resp -> resp.bodyToMono(ReqResp.class)
                        .map(body -> {
                            boolean data = (Boolean) body.getData();
                            String msg = body.getMsg();
                            return ResponseEntity.status(resp.rawStatusCode()).body(new ReqResp<>(data, msg));
                        }));
    }

    public Mono<ResponseEntity<ReqResp<BookmarkModel>>> getBookmark(String id, String jwtToken) {
        String userID = jwtService.getUserID(jwtToken);

        WebClient client = WebClient.create(URLs.DB_HOST(8000) + URLs.BKM_DELETE_GET(id));

        return client.get().header("Authorization", userID)
                .exchangeToMono(resp -> resp.bodyToMono(ReqResp.class)
                        .map(body -> {
                            BookmarkModel data = (BookmarkModel) body.getData();
                            String msg = body.getMsg();
                            return ResponseEntity.status(resp.rawStatusCode()).body(new ReqResp<>(data, msg));
                        }));
    }

    public Flux<ResponseEntity<ReqResp<BookmarkModel>>> getBookmarkFromDir(String dirID, String jwtToken) {
        String userID = jwtService.getUserID(jwtToken);
        WebClient client = WebClient.create(URLs.DB_HOST(8000) + URLs.BKMS_FROM_DIR(dirID));
        return client.get().header("Authorization", userID)
                .exchangeToFlux(resp -> {
                    return resp.bodyToFlux(ReqResp.class)
                            .map(reqResp -> {
                                BookmarkModel data = (BookmarkModel) reqResp.getData();
                                String msg = reqResp.getMsg();
                                return ResponseEntity.status(resp.rawStatusCode()).body(new ReqResp<>(data, msg));
                            });
                })
                ;
    }
}
