package com.advbkm.db.service;


import com.advbkm.db.models.entities.EntityConnector;
import com.advbkm.db.models.entities.EntityDir;
import com.advbkm.db.models.exception.ResponseException;
import com.advbkm.db.repo.RepoBookmark;
import com.advbkm.db.repo.RepoConnector;
import com.advbkm.db.repo.RepoDir;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
@Service
public class DirService {

    private final RepoDir dirRepo;
    private final RepoConnector connectorRepo;
    private final RepoBookmark bookmarkRepo;

    public DirService(RepoDir dirRepo, RepoConnector connectorRepo, RepoBookmark bookmarkRepo) {
        this.dirRepo = dirRepo;
        this.connectorRepo = connectorRepo;
        this.bookmarkRepo = bookmarkRepo;
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
        /*

        1. Get targerDir, validate creatorID and userID, must exist
        2. Get Connector, must exist
        3. Get parentID (if sub-dir), may or may-not exist. Throw error if it doesn't exist when it should, validate creatorID and userID of parentDir
        4. Store childrenID, Bookmarks
        5. Iterate bookmarks and delete them
        6. Modify Connector to remove the deleted bookmarks (WE WILL SAVE THIS ENTITY AT THE FINALLY SECTION)
        7. Check if parentDir is valid or not

        IF VALID:
        1. Iterate childrenID and get them.
        2. Update their parent to parentDir ID
        3. Save them back
        4. Update parentDir to have all the children
        5. Save parentDir
        6. Delete targetDir

        IF NOT VALID:
        1. Iterate childrenID and get them.
        2. Set their parent to "".
        3. Save them back
        4. Update connector to have all children as rootDir
        5. Delete TargetDir


        FINALLY:
        Save connector to db
         */

        final String mapTargetDir = "targetDir";
        final String mapConn = "foundConn";
        final String mapParentDir = "parentDir";
        final String mapChildrenID = "targetChildrenID";
        final String mapBookmarks = "targetBookmarksID";

        //Get target Dir and save it
        var finalMono = dirRepo.findById(dirID)
                .switchIfEmpty(Mono.error(new ResponseException("Directory not found", 404)))
                //Save targetDir to map after basic validation
                .flatMap(targetDir -> {
                    if (!targetDir.getCreatorID().equalsIgnoreCase(userID))
                        return Mono.error(new ResponseException("UserID and Dir CreatorID do not match", 401));
                    Map<String, Object> map = new HashMap<>();
                    map.put(mapTargetDir, targetDir);
                    return Mono.just(map);
                })
                //Get Connector and save it
                .flatMap(map -> {
                    return connectorRepo.findById(userID)
                            .switchIfEmpty(Mono.error(new ResponseException("Connector not found", 404)))
                            .map(conn -> {
                                map.put(mapConn, conn);
                                return map;
                            });
                })
                //Get parentDir and save ONLY IF targetDir is a sub dir
                .flatMap(map -> {
                    EntityDir dir = (EntityDir) map.get(mapTargetDir);
                    boolean hasParent = dir.getParent() != null && !dir.getParent().isEmpty();
                    if (hasParent) {
                        Mono<Map<String, Object>> updatedMap = dirRepo.findById(dir.getParent())
                                .switchIfEmpty(Mono.error(new ResponseException("Parent Dir not found when it should have existed", 404)))
                                .map(parentDir -> {
                                    map.put(mapParentDir, parentDir);
                                    return map;
                                });
                        return updatedMap;
                    }
                    return Mono.just(map);
                })
                //Store childrenID and Bookmarks
                .map(map -> {
                    EntityDir dir = (EntityDir) map.get(mapTargetDir);
                    map.put(mapBookmarks, dir.getBookmarks());
                    map.put(mapChildrenID, dir.getChildren());
                    return map;
                })
                //Iterate Over bookmarks and delete them
                .flatMap(map -> {
                    List<String> bookmarks = (List<String>) map.get(mapBookmarks);

                    var updatedMap = bookmarkRepo.deleteAllById(bookmarks)
                            .map(unused -> map);
                    return updatedMap;

                })
                //Update connector by removing the bookmarks. WE WILL UPDATE CONNECTOR AT THE END OF CHAIN BECAUSE WE MIGHT HAVE SOME OTHER UPDATING TO DO ON IT
                .map(map -> {
                    EntityConnector conn = (EntityConnector) map.get(mapConn);
                    List<String> bookmarks = (List<String>) map.get(mapBookmarks);
                    conn.getBookmarks().removeAll(bookmarks);
                    return map;
                })
                // Update Children ParentID and saving it back to database or ""
                .flatMap(map -> {
                    List<String> childrenIDs = (List<String>) map.get(mapChildrenID);
                    if (map.containsKey(mapParentDir)) {
                        EntityDir parentDir = (EntityDir) map.get(mapParentDir);
                        return dirRepo.findAllById(childrenIDs)
                                //Update the parentID of each child
                                .map(child -> {
                                    child.setParent(parentDir.get_id());
                                    return child;
                                })
                                //After updating local values, save it back
                                .flatMap(updatedLocalChild -> {
                                    return dirRepo.save(updatedLocalChild);
                                })
                                //Convert the flux to mono
                                .collectList()
                                .map(entityDirs -> map);
                    } else {
                        return dirRepo.findAllById(childrenIDs)
                                //Update the parentID to ""
                                .map(child -> {
                                    child.setParent("");
                                    return child;
                                })
                                //After updating local values save it to db
                                .flatMap(updatedLocalChild -> {
                                    return dirRepo.save(updatedLocalChild);
                                })
                                //Convert flux to mono
                                .collectList()
                                .map(entityDirs -> map);
                    }
                })
                //Update parentDir to have all childrenID if parentDir valid, else update connector to have all childrenID as rootDirs
                .map(map -> {
                    List<String> childrenIDs = (List<String>) map.get(mapChildrenID);
                    if (map.containsKey(mapParentDir)) {
                        EntityDir parentDir = (EntityDir) map.get(mapParentDir);
                        parentDir.getChildren().addAll(childrenIDs);
                    } else {
                        EntityConnector conn = (EntityConnector) map.get(mapConn);
                        conn.getRootDirs().addAll(childrenIDs);
                    }
                    return map;
                })
                //Save the updated parentDir to db if parentDir is valid, else save connector to db
                .flatMap(map -> {
                    if (map.containsKey(mapParentDir)) {
                        EntityDir parentDir = (EntityDir) map.get(mapParentDir);
                        return dirRepo.save(parentDir)
                                .map(savedParentDir -> (EntityDir) map.get(mapTargetDir));
                    } else {
                        EntityConnector connector = (EntityConnector) map.get(mapConn);
                        return connectorRepo.save(connector)
                                .map(savedConn -> (EntityDir) map.get(mapTargetDir));
                    }
                })
                //Delete the targetDir finally
                .flatMap(
                        targetDir ->
                                dirRepo.deleteById(targetDir.get_id())
                                        .map(unused -> true)

                );

        return finalMono;

    }


    public Mono<Boolean> moveDir() {
        return null;
    }

    public Mono<Boolean> moveBookmark() {
        return null;
    }

}

