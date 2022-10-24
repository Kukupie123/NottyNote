package com.advbkm.db.service;


import com.advbkm.db.models.entities.EntityConnectorUserToDir;
import com.advbkm.db.models.entities.EntityDir;
import com.advbkm.db.repo.RepoConnectorUserToDir;
import com.advbkm.db.repo.RepoDir;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;

@Service
public class DirService {

    private final RepoDir dirRepo;
    private final RepoConnectorUserToDir connectorUserToDirRepo;

    public DirService(RepoDir dirRepo, RepoConnectorUserToDir connectorUserToDirRepo) {
        this.dirRepo = dirRepo;
        this.connectorUserToDirRepo = connectorUserToDirRepo;
    }


    public Mono<EntityDir> createDir(EntityDir dir) {
        /*
        The createdBy has to be set by the gateway as this service does not deal with extracting userID from JWT token. That is done by JWT token of Gateway Service
        1. If no parent the dir is supposed to be a root dir and has to be put in the Connector_user2Dir table as well
        2. In user2Dir we are going to add to the array list if such record already exists
         */

        //Validate dir object
        if (dir.getName() == null || dir.getName().isEmpty() || dir.getCreatorID() == null || dir.getCreatorID().isEmpty())
            return Mono.error(new Exception("Null Dir fields"));

        //Set id to null to generate random ID and do other process
        dir.set_id(null);
        dir.setBookmarks(new ArrayList<>()); //Newly created dir don't have bookmarks
        dir.setChildren(new ArrayList<>()); // Newly created dir don't have children
        dir.setParent("");


        //IMPORTANT !!! Tried to store save()'s return value as a variable and then do things but DOESN'T WORK! KEEP IN MIND. YOU NEED TO DO IT ON THE SAME CHAIN
        var a = dirRepo.save(dir)
                .doOnSuccess(savedDir -> {
                    //Once we have saved we want to check if its root dir or not and if its root then we need to add the record to Connector DB as well

                    if (!(savedDir.getParent() == null || savedDir.getParent().isEmpty())) {
                        System.out.println("Not a parent ");
                        return;
                    }

                    System.out.println("ITS A PARENT");
                    //If parents don't exist we are going to add them to connector DB
                    //Check if a record with the userID is already present in Connector Collection
                    connectorUserToDirRepo.existsById(savedDir.getCreatorID())
                            .doOnSuccess(exist -> {
                                System.out.println("EZEZ EZ ");
                                if (exist) {
                                    //Record exists, we need to get the record -> add the new dir to dirs[] and update the record
                                    System.out.println("User has root dirs already");

                                    //Get the record
                                    connectorUserToDirRepo.findById(savedDir.getCreatorID())
                                            .doOnSuccess(connectorUserToDir -> {

                                                //Update dirs by adding new dir ID
                                                var newList = connectorUserToDir.getDirs();
                                                newList.add(savedDir.get_id());
                                                connectorUserToDir.setDirs(newList);

                                                //Save the new updated doc to db
                                                connectorUserToDirRepo.save(connectorUserToDir);
                                                //TODO: Add a fallback

                                            });
                                }
                                //Record doesn't exist so we can add a new one
                                System.out.println("No previous record in connector table so creating a new one");
                                var newList = new ArrayList<String>();
                                newList.add(savedDir.get_id());
                                connectorUserToDirRepo.save(new EntityConnectorUserToDir(savedDir.getCreatorID(), newList));

                            });

                });
        return a;

    }
}
