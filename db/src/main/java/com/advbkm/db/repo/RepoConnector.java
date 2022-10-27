package com.advbkm.db.repo;


import com.advbkm.db.models.entities.EntityConnector;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepoConnector extends ReactiveMongoRepository<EntityConnector, String> {
}
