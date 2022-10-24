package com.advbkm.db.service;


import com.advbkm.db.models.entities.EntityUser;
import com.advbkm.db.repo.RepoUsers;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UserService {
    private final RepoUsers userRepo;
    private final EntityUser dummyBean;

    public UserService(RepoUsers userRepo, EntityUser dummyBean) {
        this.userRepo = userRepo;
        this.dummyBean = dummyBean;
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
        Mono<EntityUser> monoUser = userRepo.insert(new EntityUser(user.getEmail(), user.getPassword(), user.getName(), user.getType()))
                .onErrorMap(err -> new Exception(err.getMessage()))
                .defaultIfEmpty(dummyBean); //Same as above

        return monoUser;


    }
}
