package com.advbkm.db.service;


import com.advbkm.db.models.entities.EntityBookmark;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class BookmarkService {

    public Mono<String> createBookmark(EntityBookmark bookmark, String userID) {

        return Mono.empty();
        //Get Dir based in DirID

    }
}

//TODO: Combine all connector into one collection as they all have userID as their PK