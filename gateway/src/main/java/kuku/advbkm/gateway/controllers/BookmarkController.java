package kuku.advbkm.gateway.controllers;

import kuku.advbkm.gateway.models.BookmarkModel;
import kuku.advbkm.gateway.models.ReqResp.ReqResp;
import kuku.advbkm.gateway.service.DbBookmarkService;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@CrossOrigin(value = "**") //Allow all origin, all headers, All Http methods.
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

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<ReqResp<Boolean>>> deleteBookmark(@PathVariable String id, @RequestHeader("Authorization") String token) {
        String jwtToken = token.substring(7);
        return bookmarkService.deleteBookmark(id, jwtToken);
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<ReqResp<BookmarkModel>>> getBookmark(@PathVariable String id, @RequestHeader("Authorization") String token) {
        String jwtToken = token.substring(7);
        return bookmarkService.getBookmark(id, jwtToken);
    }

    @GetMapping("/dir/{dirID}")
    public ResponseEntity<Flux<ReqResp<BookmarkModel>>> getBookmarksForDir(@PathVariable String dirID, @RequestHeader("Authorization") String token) {
        String jwtToken = token.substring(7);
        var a = bookmarkService.getBookmarkFromDir(dirID, jwtToken);
        return ResponseEntity.ok(a);
    }
}
