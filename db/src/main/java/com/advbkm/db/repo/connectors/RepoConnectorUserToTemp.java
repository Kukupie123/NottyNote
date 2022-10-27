package com.advbkm.db.repo.connectors;

import com.advbkm.db.models.entities.connectorEntity.EntityConnectorUserToTemp;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepoConnectorUserToTemp extends ReactiveMongoRepository<EntityConnectorUserToTemp, String> {
}
