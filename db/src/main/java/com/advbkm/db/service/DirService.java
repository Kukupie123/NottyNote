package com.advbkm.db.service;


import com.advbkm.db.models.entities.EntityConnectorUserToDir;
import com.advbkm.db.models.entities.EntityDir;
import com.advbkm.db.repo.RepoConnectorUserToDir;
import com.advbkm.db.repo.RepoDir;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Log4j2
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


        //IMPORTANT : Nested Mono operations do not work if you don't make it return from the mono actions inside the nest operations. It makes it NOT WAIT for the result from that mono publisher and directly returns
        Mono<EntityDir> savedDirMono = dirRepo.save(dir); //Save in db

        //We need to check if the newly saved entityDir needs to be saved to another Table known as Connector, This is done inside this flatmap chain
        return savedDirMono.flatMap(

                savedDir -> {

                    //We need to add savedDir to Connector if this is true
                    if ((savedDir.getParent() == null || savedDir.getParent().isEmpty())) {

                        //Check if a record already exists with same userID, if yes then we need to get it from table and update it's values and then AGAIN save it to connector table
                        return connectorUserToDirRepo
                                .findById(savedDir.getCreatorID())
                                .defaultIfEmpty(new EntityConnectorUserToDir(null, null))
                                .flatMap(savedConnector -> {

                                    //If this is true then no record found so we can simply save new record
                                    if (savedConnector.getUserID() == null || savedConnector.getUserID().isEmpty()) {

                                        List<String> stringList = new ArrayList<>();
                                        stringList.add(savedDir.get_id());

                                        //Save to other db and flatmap it and return it to upper part of the nest
                                        return connectorUserToDirRepo.save(new EntityConnectorUserToDir(savedDir.getCreatorID(), stringList))
                                                .map(connectorUserToDir -> savedDir);
                                    } else {


                                        //Record exists we need to update it's dir list and then save it again
                                        List<String> stringList = savedConnector.getDirs();
                                        stringList.add(savedDir.get_id());

                                        //Save to connector db, flatmap it and return it to upper part of nest
                                        return connectorUserToDirRepo.save(savedConnector)
                                                .map(connectorUserToDir -> savedDir);
                                    }

                                });
                    } else {

                        //not a parent so no need to do further
                        log.info("NOT A PARENT");
                        return Mono.justOrEmpty(savedDir);
                    }

                }
        );


    }
}
