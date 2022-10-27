package com.advbkm.db.service;


import com.advbkm.db.models.entities.EntityConnector;
import com.advbkm.db.models.entities.EntityDir;
import com.advbkm.db.models.entities.EntityUser;
import com.advbkm.db.repo.RepoConnector;
import com.advbkm.db.repo.RepoDir;
import com.advbkm.db.repo.RepoUsers;
import com.advbkm.db.repo.connectors.RepoConnectorUserToDir;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Log4j2
@Service
public class DirService {

    private final RepoDir dirRepo;
    private final RepoConnectorUserToDir connectorUserToDirRepo;
    private final RepoUsers userRepo;
    private final RepoConnector connectorRepo;

    public DirService(RepoDir dirRepo, RepoConnectorUserToDir connectorUserToDirRepo, RepoUsers userRepo, RepoConnector connectorRepo) {
        this.dirRepo = dirRepo;
        this.connectorUserToDirRepo = connectorUserToDirRepo;
        this.userRepo = userRepo;
        this.connectorRepo = connectorRepo;
    }


    public Mono<String> createDir(EntityDir dir, String userID) {

        //Names of map, map that we will create inside mono to save object that will otherwise be lost during mono/flux transformation
        String mapParentDirName = "parentDir";
        String mapSavedDirName = "savedDir";
        String mapFoundConnectorName = "foundConnector";


        //Set id to null to generate random ID and do other process
        dir.set_id(null);
        dir.setBookmarks(new ArrayList<>()); //Newly created dir don't have bookmarks
        dir.setChildren(new ArrayList<>()); // Newly created dir don't have children
        dir.setCreatorID(userID);

        //Validate dir object
        if (dir.getName() == null || dir.getName().isEmpty() || dir.getCreatorID() == null || dir.getCreatorID().isEmpty())
            return Mono.error(new Exception("Null Dir fields"));

        /*
        1. Check if it's parent dir or sub dir

        IF SUB DIR-------------
        1. Get parent DIR and validate if it's correct. Throw error if not correct
        2. Save the dir (Lets call it savedDir)
        3. add the savedDir.getID() to parentDir.getChildren()
        4. save the parentDir


        IF PARENT DIR-----------
        1. Get connector entity using the creatorID. If none found create a new Connector Entity using defaultIfEmpty()
        2. Save the dir (Lets call it savedDir)
        3. add savedDir.getID to connector.getRootDirs()
        4. Save the updated connector
         */

        //TODO: Transaction based I/O

        boolean hasParent = !(dir.getParent() == null || dir.getParent().isEmpty());

        if (hasParent) {

            return dirRepo.findById(dir.getParent()).defaultIfEmpty(new EntityDir())
                    //Get parentDIR
                    .flatMap(parentDir -> {

                        if (parentDir.get_id() == null || parentDir.get_id().isEmpty())
                            return Mono.error(new Exception("Parent ID is not valid"));

                        //Create a map to store parentDir object
                        return Mono.just(parentDir);
                    })
                    //Save dir
                    .flatMap(parentDir -> {

                        //Save the dir
                        Mono<EntityDir> savedDirMono = dirRepo.save(dir);

                        Mono<Map<String, Object>> mapMono = savedDirMono.map(savedDir -> {

                            //Store parentDir and savedDir in a map and return it
                            HashMap<String, Object> map = new HashMap<>();
                            map.put(mapParentDirName, parentDir);
                            map.put(mapSavedDirName, savedDir);

                            return map;
                        });

                        return mapMono;
                    })
                    //Update Children list of parent
                    .map(map -> {

                        //Update children list of parentDir
                        var parentDir = (EntityDir) map.get(mapParentDirName);
                        var savedDir = (EntityDir) map.get(mapSavedDirName);

                        parentDir.getChildren().add(savedDir.get_id());

                        return map;
                    })
                    //Save updated parentDir
                    .flatMap(map -> {

                        //Save the updated parent
                        EntityDir parentDir = (EntityDir) map.get(mapParentDirName);
                        EntityDir savedDir = (EntityDir) map.get(mapSavedDirName);

                        return dirRepo.save(parentDir)
                                .map(savedParent -> savedDir.get_id());


                    });

        }

        return connectorRepo.findById(userID).defaultIfEmpty(new EntityConnector(userID))
                //Save the dir, return a map with foundConnector and savedDir
                .flatMap(foundConnector -> {

                    return dirRepo.save(dir)
                            .map(savedDir -> {

                                HashMap<String, Object> map = new HashMap<>();
                                map.put(mapSavedDirName, savedDir);
                                map.put(mapFoundConnectorName, foundConnector);
                                return map;
                            });
                })
                //Update the values of connector
                .map(map -> {

                    EntityConnector foundConnector = (EntityConnector) map.get(mapFoundConnectorName);
                    EntityDir savedDir = (EntityDir) map.get(mapSavedDirName);
                    foundConnector.getRootDirs().add(savedDir.get_id());
                    return map;
                })
                //Save the updated Connector
                .flatMap(map -> {

                    return connectorRepo.save((EntityConnector) map.get(mapFoundConnectorName))
                            .map(updatedConnector -> ((EntityDir) map.get(mapSavedDirName)).get_id());
                });


    }

    public Mono<Boolean> deleteDir(String dirID, String userID) {

        //Get the user
        return userRepo.findById(userID).defaultIfEmpty(new EntityUser())
                .flatMap(foundUser -> {

                    //Check if userID is valid
                    if (foundUser.getEmail() == null || foundUser.getEmail().isEmpty()) {
                        return Mono.error(new Exception("User ID is invalid"));
                    }

                    // Get the dirEntity and check if it's creatorID match OR if userID is of admin type

                    //Access the dir we are going to delete for several reasons
                    return dirRepo.findById(dirID) //Mono<Boolean> type
                            .defaultIfEmpty(new EntityDir())
                            .flatMap(foundDir -> {

                                if (foundDir.get_id() == null || foundDir.get_id().isEmpty())
                                    return Mono.error(new FileNotFoundException("Directory with ID " + dirID + " not found"));

                                var a = foundUser.getType().split(",");


                                //Check if foundUser has right to delete the folder

                                log.info("userID in dir is {} , and userID in user is {}", foundDir.getCreatorID(), foundUser.getEmail());

                                if (!foundDir.getCreatorID().equalsIgnoreCase(foundUser.getEmail())) {

                                    if (Arrays.asList(a).contains("ADMIN") == false) {
                                        return Mono.error(new Exception("UserID of Requester do not match creatorID of deleting folder OR NO ADMIN RIGHTS"));
                                    }
                                }
                                //Check if it has parent
                                if (foundDir.getParent() == null || foundDir.getParent().isEmpty()) {
                                    //foundDir is root dir and has no parent

                                    //Get connector record, to remove foundDir ID from the dirs[] list and add foundDir children to the dirs[] list
                                    return connectorUserToDirRepo.findById(foundDir.getCreatorID()) //Mono<Boolean> type
                                            .flatMap(foundConnector -> {

                                                //Check if foundDir has children
                                                if (!(foundDir.getChildren() == null || foundDir.getChildren().isEmpty())) {

                                                    //Children exist

                                                    //We need to set the parentID of children to empty string as they are now root and have no parent
                                                    return Flux.fromArray(foundDir.getChildren().toArray())
                                                            .map(s -> (String) s)
                                                            .flatMap(childID -> {

                                                                //Access the child based on their ID
                                                                return dirRepo.findById(childID)
                                                                        .flatMap(child -> {

                                                                            //Set parent to empty string
                                                                            child.setParent("");

                                                                            //Save the updated value to table
                                                                            return dirRepo.save(child)
                                                                                    .map(updatedChild -> updatedChild.get_id());
                                                                        });
                                                            })
                                                            //After updating the values we can now update connector table and add these children ID as root for the creatorID
                                                            .collectList()
                                                            .flatMap(childrenIDs -> {

                                                                foundConnector.getDirs().addAll(childrenIDs);
                                                                foundConnector.getDirs().remove(foundDir.get_id());

                                                                return connectorUserToDirRepo.save(foundConnector)
                                                                        .flatMap(updatedConnector -> {

                                                                            //Remove foundDir from record
                                                                            return dirRepo.deleteById(foundDir.get_id())
                                                                                    .map(e -> true);
                                                                        });
                                                            });
                                                }

                                                foundConnector.getDirs().remove(foundDir.get_id()); // remove foundDir's ID from root dirs list
                                                //Save the updated foundConnector
                                                return connectorUserToDirRepo.save(foundConnector)
                                                        .flatMap(updatedConnector -> {

                                                            //remove foundDir from record
                                                            return dirRepo.deleteById(foundDir.get_id())
                                                                    .map(e -> true);
                                                        });

                                            });
                                } else {
                                    //foundDir is sub dir

                                    //Get parent before doing anything because we will need it in several tasks
                                    return dirRepo.findById(foundDir.getParent()) //Mono<Boolean> type
                                            .flatMap(parentDir -> {

                                                if (!(foundDir.getChildren() == null || foundDir.getChildren().isEmpty())) {

                                                    //Children exist and foundDir is a sub dir

                                                    //Create flux of children so that we can update their parent ID to the parent ID of foundDir
                                                    return Flux.fromArray(foundDir.getChildren().toArray()) //Mono<Boolean> type
                                                            .map(s -> (String) s)
                                                            .flatMap(childID -> {

                                                                //Get the dir entity and update it's parent value
                                                                return dirRepo.findById(childID) //Get the child

                                                                        .flatMap(childEntity -> {

                                                                            //Update the parent ID and save them again
                                                                            childEntity.setParent(parentDir.get_id());

                                                                            return dirRepo.save(childEntity).map(e -> childID);
                                                                        });


                                                            })
                                                            .collectList()// (Mono<List<String>>) type. Collect the childrenID as list after updating it's parentID values in database so that we can update parent's children list
                                                            .flatMap(strings -> {

                                                                //Update parent's children value
                                                                parentDir.getChildren().remove(foundDir.get_id());
                                                                parentDir.getChildren().addAll(strings);

                                                                //Now save it back and remove sub dir from record
                                                                return dirRepo.save(parentDir)
                                                                        .flatMap(updatedParentDir -> {

                                                                            //Remove foundDir from record
                                                                            return dirRepo.deleteById(foundDir.get_id()).map(e -> true);
                                                                        });
                                                            });


                                                } else {

                                                    //Child do not exist and foundDir is a sub dir

                                                    //remove foundDir dirID from parent's children list
                                                    parentDir.getChildren().remove(foundDir.get_id());
                                                    return dirRepo.save(parentDir)
                                                            .flatMap(updatedParentDir -> {

                                                                //Remove foundDir From record
                                                                return dirRepo.deleteById(foundDir.get_id()).map(e -> true);
                                                            });
                                                }
                                            });


                                }
                            });
                });

    }

    public Mono<Boolean> moveDir() {
        return null;
    }

    public Mono<Boolean> moveBookmark() {
        return null;
    }

}

