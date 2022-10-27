package com.advbkm.db.repo;

import com.advbkm.db.models.entities.EntityBookmark;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface RepoBookmark extends ReactiveMongoRepository<EntityBookmark, String> {
}
