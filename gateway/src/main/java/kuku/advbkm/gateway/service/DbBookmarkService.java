package kuku.advbkm.gateway.service;


import kuku.advbkm.gateway.models.BookmarkModel;
import kuku.advbkm.gateway.models.ReqResp.ReqResp;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import util.urls.URLs;

@Service
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
                .exchangeToMono(resp -> {
                    return resp.bodyToMono(ReqResp.class)
                            .map(body -> {
                                String id = (String) body.getData();
                                String msg = body.getMsg();
                                return ResponseEntity.status(resp.rawStatusCode()).body(new ReqResp<>(id, msg));
                            });
                });

    }
}
