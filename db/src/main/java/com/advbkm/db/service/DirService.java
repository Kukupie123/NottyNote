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
        The createdBy has to be set before it was passed as the argument
        The service that handles JWT service needs to take care of extracting the username from the JWT Token
         */

        //Validate dir object
        if (dir.getName() == null || dir.getName().isEmpty() || dir.getCreatorID() == null || dir.getCreatorID().isEmpty())
            return Mono.error(new Exception("Null Dir fields"));

        //Set id to null to generate random ID and do other process
        dir.set_id(null);
        dir.setBookmarks(new ArrayList<>()); //Newly created dir don't have bookmarks
        dir.setChildren(new ArrayList<>()); // Newly created dir don't have children

        //TODO: Transaction based I/O

        //Check if it's root dir or sub dir
        if (dir.getParent() == null || dir.getParent().isEmpty()) {
            //It is a root DIR since it doesn't have a parent

            return dirRepo.save(dir)
                    .flatMap(savedDir -> {

                        //Check if connector table already has a record
                        return connectorUserToDirRepo.findById(savedDir.getCreatorID()).defaultIfEmpty(new EntityConnectorUserToDir(null, null))
                                .flatMap(savedConnector -> {

                                    //Check if record exist
                                    if (savedConnector.getUserID() == null) {

                                        //No record found, simply save it
                                        List<String> stringList = new ArrayList<>();
                                        stringList.add(savedDir.get_id());
                                        return connectorUserToDirRepo.save(new EntityConnectorUserToDir(savedDir.getCreatorID(), stringList))
                                                .map(e -> savedDir);
                                    } else {

                                        //Record found, update dir value and save it again
                                        var stringList = savedConnector.getDirs();
                                        stringList.add(savedDir.getCreatorID());
                                        return connectorUserToDirRepo.save(savedConnector)
                                                .map(e -> savedDir);
                                    }
                                });

                    });
        } else {
            //It is a sub dir since it has parentID, Check if a dir with parentID exists
            return dirRepo.findById(dir.getParent()).defaultIfEmpty(new EntityDir(null, null, null, null, null, null, null))
                    .flatMap(parentDir -> {

                        //Check if parent is valid
                        if (parentDir.get_id() == null) {

                            //Parent not valid throw error
                            return Mono.error(new Exception("Invalid Parent ID supplied"));
                        } else {

                            //Parent is valid, save the sub dir and then update parent's children list
                            return dirRepo.save(dir)
                                    .flatMap(savedDir -> {

                                        //After saving sub-dir, update children list and update parent and return back sub-dir NOT parent
                                        var updatedChildren = parentDir.getChildren();
                                        updatedChildren.add(savedDir.get_id());
                                        return dirRepo.save(parentDir)
                                                .map(e -> savedDir); //return sub-dir not parent
                                    });
                        }
                    });

        }


    }
}
