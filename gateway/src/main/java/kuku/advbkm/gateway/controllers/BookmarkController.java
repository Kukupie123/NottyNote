package kuku.advbkm.gateway.controllers;

import kuku.advbkm.gateway.models.BookmarkModel;
import kuku.advbkm.gateway.models.ReqResp.ReqResp;
import kuku.advbkm.gateway.service.DbBookmarkService;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Log4j2
@RestController
@RequestMapping("/api/v1/gate/bookmark/")
public class BookmarkController {

    final private DbBookmarkService bookmarkService;

    public BookmarkController(DbBookmarkService bookmarkService) {
        this.bookmarkService = bookmarkService;
    }


    @PostMapping("/create")
    public Mono<ResponseEntity<ReqResp<String>>> createBookmark(@RequestBody BookmarkModel bookmarkModel, @RequestHeader("Authorization") String token) {
        String jwtToken = token.substring(7);
        return bookmarkService.createBookmark(bookmarkModel, jwtToken);
    }
}
