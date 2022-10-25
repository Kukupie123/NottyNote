package com.advbkm.db.service;


import com.advbkm.db.models.entities.EntityConnectorUserToDir;
import com.advbkm.db.models.entities.EntityDir;
import com.advbkm.db.repo.RepoConnectorUserToDir;
import com.advbkm.db.repo.RepoDir;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
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
                        return connectorUserToDirRepo.findById(savedDir.getCreatorID()).defaultIfEmpty(new EntityConnectorUserToDir())
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
                                        stringList.add(savedDir.get_id());
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

    public Mono<Boolean> deleteDir(String id) {

        //Get the dirEntity, Check if it has parents
        //If it has parent then get parent dir, remove the dir from children list, save parent, delete the dir
        //if it doesn't have parent get connector of user, remove dir from dir lists, save connector entity, delete dir
        //Oh and we also have to do something if the dir we are deleting have children, easy thing would be to get those children and modify their parents to be the parent of the deleting dir. If deleting dir doesn't have parent then of them will be set as root

        //Access the dir we are going to delete for several reasons
        return dirRepo.findById(id) //Mono<Boolean> type
                .flatMap(foundDir -> {

                    //Check if it has parent
                    if (foundDir.getParent() == null || foundDir.getParent().isEmpty()) {
                        //foundDir is root dir

                        //Get connector record, to remove foundDir ID from the dirs[] list and add foundDir children to the dirs[] list
                        return connectorUserToDirRepo.findById(foundDir.getCreatorID()) //Mono<Boolean> type
                                .flatMap(foundConnector -> {

                                    //Check if foundDir has children
                                    if (!(foundDir.getChildren() == null || foundDir.getChildren().isEmpty())) {

                                        //Children exist

                                        //save children in a list
                                        List<String> children = foundDir.getChildren();

                                        foundConnector.getDirs().addAll(children); //update the children list by adding the children of foundDir as they are now considered root dirs too
                                    }

                                    foundConnector.getDirs().remove(foundDir.get_id()); // remove foundDir's ID from root dirs list
                                    //Save the updated foundConnector
                                    return connectorUserToDirRepo.save(foundConnector)
                                            .flatMap(updatedConnector -> {

                                                //remove foundDir from record
                                                return dirRepo.deleteById(foundDir.get_id()).map(e -> true);
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

                                        //remove foundDir id from parent's children list
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
    }

    public Mono<Boolean> moveDir() {
        return null;
    }

    public Mono<Boolean> moveBookmark() {
        return null;
    }

}
