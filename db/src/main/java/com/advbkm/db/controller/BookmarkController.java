package com.advbkm.db.controller;


import com.advbkm.db.models.entities.EntityBookmark;
import com.advbkm.db.models.reqresp.ReqResp;
import com.advbkm.db.service.BookmarkService;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Log4j2
@RestController()
@RequestMapping("/api/v1/db/bookmark")
public class BookmarkController {
    private final BookmarkService bookmarkService;

    public BookmarkController(BookmarkService bookmarkService) {
        this.bookmarkService = bookmarkService;
    }

    /**
     * Create a new bookmark
     * @return Bookmark ID of the newly created bookmark
     */
    @PostMapping("/create")
    public Mono<ResponseEntity<ReqResp<String>>> createBookmark(@RequestBody EntityBookmark bookmark, @RequestHeader("Authorization") String userID) {
        log.info("Create bookmark with body {}", bookmark);
        return bookmarkService.createBookmark(bookmark, userID)
                .map(
                        bkmID -> ResponseEntity.ok().body(new ReqResp<>(bkmID, "Success"))
                )
                ;

    }

    /**
     * Delete the bookmark with the given ID
     * @return True if successful, false if something went wrong
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<ReqResp<Boolean>>> deleteBookmark(@PathVariable String id, @RequestHeader("Authorization") String userID) {
        return bookmarkService.deleteBookmark(id, userID)
                .map(aBoolean -> ResponseEntity.ok(new ReqResp<>(aBoolean, "Success")));
    }

    /**
     * Get the bookmark with the given ID
     * @return Bookmark Entity as JSON
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<ReqResp<EntityBookmark>>> getBookmark(@PathVariable String id, @RequestHeader("Authorization") String userID) {
        return bookmarkService.getBookmark(id, userID)
                .map(bookmark -> ResponseEntity.ok(new ReqResp<>(bookmark, "")))
                ;
    }

    /**
     * Return an Array of Bookmarks that are inside the dirID
     * @return [{data : "bookmark", msg : ""}]
     */
    @GetMapping("/dir/{dirID}")
    public ResponseEntity<Flux<ReqResp<EntityBookmark>>> getBookmarksFromDir(@PathVariable String dirID, @RequestHeader("Authorization") String userID) {
        var a = bookmarkService.getBookmarksFromDir(dirID, userID)
                .map(bookmark -> new ReqResp<>(bookmark, ""));
        return ResponseEntity.ok(a);
    }
}
