package com.advbkm.db.repo.connectors;


import com.advbkm.db.models.entities.connectorEntity.EntityConnectorUserToBookmark;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepoConnectorUserToBookmark extends ReactiveMongoRepository<EntityConnectorUserToBookmark, String> {
}
