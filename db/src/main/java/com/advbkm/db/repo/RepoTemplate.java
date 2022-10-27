package com.advbkm.db.repo;


import com.advbkm.db.models.entities.TemplateEntity.EntityTemplate;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepoTemplate extends ReactiveMongoRepository<EntityTemplate, String> {
}
