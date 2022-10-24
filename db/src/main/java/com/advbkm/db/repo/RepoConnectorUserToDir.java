package com.advbkm.db.repo;

import com.advbkm.db.models.entities.EntityConnectorUserToDir;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface RepoConnectorUserToDir extends ReactiveMongoRepository<EntityConnectorUserToDir, String> {
}
