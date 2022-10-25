package com.advbkm.db.repo;

import com.advbkm.db.models.entities.EntityConnectorUserToDir;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepoConnectorUserToDir extends ReactiveMongoRepository<EntityConnectorUserToDir, String> {
}
