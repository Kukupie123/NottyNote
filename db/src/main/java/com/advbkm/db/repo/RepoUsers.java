package com.advbkm.db.repo;

import com.advbkm.db.models.entities.EntityUser;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface RepoUsers extends ReactiveMongoRepository<EntityUser, String> {
   
}
