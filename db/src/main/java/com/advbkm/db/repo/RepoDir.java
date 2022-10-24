package com.advbkm.db.repo;

import com.advbkm.db.models.entities.EntityDir;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepoDir extends ReactiveMongoRepository<EntityDir, String> {
}
