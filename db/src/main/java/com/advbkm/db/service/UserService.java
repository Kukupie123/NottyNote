package com.advbkm.db.service;


import com.advbkm.db.models.entities.EntityUser;
import com.advbkm.db.models.exception.ResponseException;
import com.advbkm.db.repo.RepoUsers;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UserService {
    private final RepoUsers userRepo;

    public UserService(RepoUsers userRepo) {
        this.userRepo = userRepo;
    }


    public Mono<EntityUser> getUser(String id) {
        return userRepo.findById(id);
    }

    public Mono<EntityUser> createUser(EntityUser user) {
        //Validate user
        if (user.getEmail() == null || user.getPassword() == null || user.getName() == null)
            return Mono.error(new Exception("Invalid Fields"));
        if (user.getEmail().isEmpty() || user.getPassword().isEmpty() || user.getName().isEmpty())
            return Mono.error(new Exception("Empty Fields"));

        //Set user to true
        user.setType("USER");

        return userRepo.insert(new EntityUser(user.getEmail(), user.getPassword(), user.getName(), user.getType()))
                .onErrorMap(err -> new Exception(err.getMessage()))
                .switchIfEmpty(Mono.error(new ResponseException("Creating User failed", 500)));


    }
}
