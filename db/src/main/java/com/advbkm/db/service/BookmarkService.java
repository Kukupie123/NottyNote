package com.advbkm.db.service;


import com.advbkm.db.models.entities.EntityBookmark;
import com.advbkm.db.repo.RepoBookmark;
import com.advbkm.db.repo.RepoDir;
import com.advbkm.db.repo.RepoTemplate;
import com.advbkm.db.repo.connectors.RepoConnectorUserToBookmark;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class BookmarkService {
    private final RepoBookmark repoBookmark;
    private final RepoDir repoDir;
    private final RepoTemplate repoTemplate;
    private final RepoConnectorUserToBookmark repoConnectorUserToBookmark;

    public BookmarkService(RepoBookmark repoBookmark, RepoDir repoDir, RepoTemplate repoTemplate, RepoConnectorUserToBookmark repoConnectorUserToBookmark) {
        this.repoBookmark = repoBookmark;
        this.repoDir = repoDir;
        this.repoTemplate = repoTemplate;
        this.repoConnectorUserToBookmark = repoConnectorUserToBookmark;
    }

    public Mono<String> createBookmark(EntityBookmark bookmark, String userID) {


        //Get Dir based in DirID

    }
}

//TODO: Combine all connector into one collection as they all have userID as their PK