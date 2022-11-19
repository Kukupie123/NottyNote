package com.advbkm.db.service;


import com.advbkm.db.models.entities.EntityConnector;
import com.advbkm.db.models.entities.EntityDir;
import com.advbkm.db.models.exception.ResponseException;
import com.advbkm.db.repo.RepoBookmark;
import com.advbkm.db.repo.RepoConnector;
import com.advbkm.db.repo.RepoDir;
import com.advbkm.db.repo.RepoTemplate;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
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
    private final RepoTemplate templateRepo;

    public DirService(RepoDir dirRepo, RepoConnector connectorRepo, RepoBookmark bookmarkRepo, RepoTemplate templateRepo) {
        this.dirRepo = dirRepo;
        this.connectorRepo = connectorRepo;
        this.bookmarkRepo = bookmarkRepo;
        this.templateRepo = templateRepo;
    }


    public Mono<String> createDir(EntityDir dir, String userID) {

        //Names of map, map that we will create inside mono to save object that will otherwise be lost during mono/flux transformation
        String mapParentDirName = "parentDir";
        String mapSavedDirName = "savedDir";
        String mapFoundConnectorName = "foundConnector";


        //Set id to null to generate random ID and do other process
        dir.setId(null);
        dir.setBookmarks(new ArrayList<>()); //Newly created dir don't have bookmarks
        dir.setChildren(new ArrayList<>()); // Newly created dir don't have children
        dir.setCreatorID(userID);

        //Validate dir object
        if (dir.getName() == null || dir.getName().isEmpty() || dir.getCreatorID() == null || dir.getCreatorID().isEmpty())
            return Mono.error(new ResponseException("Null Dir fields", 400));

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

                        if (parentDir.getId() == null || parentDir.getId().isEmpty())
                            return Mono.error(new ResponseException("Parent ID is not valid", 404));

                        //Create a map to store parentDir object
                        return Mono.just(parentDir);
                    })
                    //Save dir
                    .flatMap(parentDir -> {

                        //Save the dir
                        Mono<EntityDir> savedDirMono = dirRepo.save(dir);

                        return savedDirMono.<Map<String, Object>>map(savedDir -> {
                            //Store parentDir and savedDir in a map and return it
                            HashMap<String, Object> map = new HashMap<>();
                            map.put(mapParentDirName, parentDir);
                            map.put(mapSavedDirName, savedDir);

                            return map;
                        });
                    })
                    //Update Children list of parent
                    .map(map -> {

                        //Update children list of parentDir
                        var parentDir = (EntityDir) map.get(mapParentDirName);
                        var savedDir = (EntityDir) map.get(mapSavedDirName);

                        parentDir.getChildren().add(savedDir.getId());

                        return map;
                    })
                    //Save updated parentDir
                    .flatMap(map -> {

                        //Save the updated parent
                        EntityDir parentDir = (EntityDir) map.get(mapParentDirName);
                        EntityDir savedDir = (EntityDir) map.get(mapSavedDirName);

                        return dirRepo.save(parentDir)
                                .map(savedParent -> savedDir.getId());


                    });

        }

        return connectorRepo.findById(userID).defaultIfEmpty(new EntityConnector(userID))
                //Save the dir, return a map with foundConnector and savedDir
                .flatMap(foundConnector -> dirRepo.save(dir)
                        .map(savedDir -> {

                            HashMap<String, Object> map = new HashMap<>();
                            map.put(mapSavedDirName, savedDir);
                            map.put(mapFoundConnectorName, foundConnector);
                            return map;
                        }))
                //Update the values of connector
                .map(map -> {

                    EntityConnector foundConnector = (EntityConnector) map.get(mapFoundConnectorName);
                    EntityDir savedDir = (EntityDir) map.get(mapSavedDirName);
                    foundConnector.getRootDirs().add(savedDir.getId());
                    return map;
                })
                //Save the updated Connector
                .flatMap(map -> connectorRepo.save((EntityConnector) map.get(mapFoundConnectorName))
                        .map(updatedConnector -> ((EntityDir) map.get(mapSavedDirName)).getId()));


    }

    public Mono<Boolean> deleteDir(String dirID, String userID) {
        log.info("delete dir service function triggered");
        /*
        1. Get targetDir, validate creatorID and userID, must exist
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
        4. Update connector to have all children as rootDir and remove targetDir ID from connector
        5. Delete TargetDir

        FINALLY:
        Save connector to db
         */

        final String mapTargetDir = "targetDir";
        final String mapConn = "foundConn";
        final String mapParentDir = "parentDir";
        final String mapChildrenID = "targetChildrenID";
        final String mapBookmarks = "targetBookmarksID";
        final String mapTemplate = "targetTemp";

        //Get target Dir and save it
        return dirRepo.findById(dirID)
                .switchIfEmpty(Mono.error(new ResponseException("Directory not found", 404)))
                //Save targetDir to map after basic validation
                .flatMap(targetDir -> {

                    if (!targetDir.getCreatorID().equalsIgnoreCase(userID))
                        return Mono.error(new ResponseException("UserID and Dir CreatorID do not match", 401));
                    Map<String, Object> map = new HashMap<>();
                    map.put(mapTargetDir, targetDir);
                    log.info("Chain 1 : Got Directory {} from db and saved it locally", targetDir);
                    return Mono.just(map);
                })
                //Get Connector and save it
                .flatMap(map -> connectorRepo.findById(userID)
                        .switchIfEmpty(Mono.error(new ResponseException("Connector not found", 404)))
                        .map(conn -> {
                            map.put(mapConn, conn);
                            log.info("Chain 2 : Got Connector {} from db and saved it locally", conn);
                            return map;
                        }))
                //Get parentDir and save ONLY IF targetDir is a sub dir
                .flatMap(map -> {
                    EntityDir dir = (EntityDir) map.get(mapTargetDir);
                    boolean hasParent = dir.getParent() != null && !dir.getParent().isEmpty();
                    if (hasParent) {
                        return dirRepo.findById(dir.getParent())
                                .switchIfEmpty(Mono.error(new ResponseException("Parent Dir not found when it should have existed", 404)))
                                .map(parentDir -> {
                                    map.put(mapParentDir, parentDir);
                                    log.info("Chain 2 : Got Parent Dir {} from db and saved it locally", parentDir);
                                    return map;
                                });
                    }
                    log.info("Chain 2 : No Parent Dir from db So returning back the original map value");
                    return Mono.just(map);
                })
                //Store childrenID and Bookmarks
                .map(map -> {
                    EntityDir dir = (EntityDir) map.get(mapTargetDir);
                    map.put(mapBookmarks, dir.getBookmarks());
                    map.put(mapChildrenID, dir.getChildren());
                    log.info("Chain 3 : Stored children {} and bookmarks {} from Deleting Dir", dir.getChildren(), dir.getBookmarks());
                    return map;
                })
                //Access each bookmark and get its templateID and save it the list to a map
                .flatMap(map -> {
                    map.put(mapTemplate, new ArrayList<String>());
                    List<String> bookmarks = (List<String>) map.get(mapBookmarks);
                    return bookmarkRepo.findAllById(bookmarks)
                            //Access it's templateID
                            .map(bookmark -> {
                                ((List<String>) map.get(mapTemplate)).add(bookmark.getTemplateID());
                                return bookmark;
                            })
                            //Collect all flux operation
                            .collectList()
                            .map(entityBookmarks -> map);
                })
                //Access each template now and remove the bookmark list, save it back
                .flatMap(map -> {
                    List<String> temps = (List<String>) map.get(mapTemplate);

                    return templateRepo.findAllById(temps)
                            //remove the bookmarks
                            .map(template -> {
                                List<String> bookmarks = (List<String>) map.get(mapBookmarks);
                                template.getBookmarks().removeAll(bookmarks);
                                return template;
                            })
                            //Save the templates
                            .flatMap(template -> templateRepo.save(template))
                            .collectList()
                            .map(entityTemplates -> map)
                            ;
                })
                //Iterate Over bookmarks and delete them
                .flatMap(map -> {
                    List<String> bookmarks = (List<String>) map.get(mapBookmarks);
                    //IMPORTANT : After deleting we can't map as we get a void,
                    //Use Then to send custom mono after your action
                    log.info("Chain 4 : Deleting all bookmarks from db in the directory's Bookmarks");
                    return bookmarkRepo.deleteAllById(bookmarks)
                            .then(Mono.just(map));
                })
                //Update connector by removing the bookmarks. WE WILL UPDATE CONNECTOR AT THE END OF CHAIN BECAUSE WE MIGHT HAVE SOME OTHER UPDATING TO DO ON IT
                .map(map -> {
                    EntityConnector conn = (EntityConnector) map.get(mapConn);
                    List<String> bookmarks = (List<String>) map.get(mapBookmarks);
                    conn.getBookmarks().removeAll(bookmarks);
                    log.info("Chain 5 : Removed bookmarks locally from Connector entity");
                    return map;
                })
                // Update Children ParentID and saving it back to database or ""
                .flatMap(map -> {
                    log.info("Updating children parentID");
                    List<String> childrenIDs = (List<String>) map.get(mapChildrenID);
                    if (map.containsKey(mapParentDir)) {
                        EntityDir parentDir = (EntityDir) map.get(mapParentDir);
                        return dirRepo.findAllById(childrenIDs)
                                //Update the parentID of each child
                                .map(child -> {
                                    child.setParent(parentDir.getId());
                                    return child;
                                })
                                //After updating local values, save it back
                                .flatMap(updatedLocalChild -> dirRepo.save(updatedLocalChild))
                                //Convert the flux to mono
                                .collectList()
                                .map(entityDirs -> {
                                    log.info("Chain 6 : Updated ParentID of Children to Deleting Dir's ParentID {} in DB", parentDir.getId());
                                    return map;
                                });
                    } else {
                        return dirRepo.findAllById(childrenIDs)
                                //Update the parentID to ""
                                .map(child -> {
                                    child.setParent("");
                                    return child;
                                })
                                //After updating local values save it to db
                                .flatMap(updatedLocalChild -> dirRepo.save(updatedLocalChild))
                                //Convert flux to mono
                                .collectList()
                                .map(entityDirs -> {
                                    log.info("Chain 6 : Updated ParentID of Children to Empty String in DB");
                                    return map;
                                });
                    }
                })
                //Update parentDir to have all childrenID if parentDir valid, else update connector to have all childrenID as rootDirs and delete targetDir ID
                .map(map -> {
                    EntityDir targetDir = (EntityDir) map.get(mapTargetDir);
                    List<String> childrenIDs = (List<String>) map.get(mapChildrenID);
                    if (map.containsKey(mapParentDir)) {
                        EntityDir parentDir = (EntityDir) map.get(mapParentDir);
                        parentDir.getChildren().addAll(childrenIDs);
                        parentDir.getChildren().remove(targetDir.getId());
                        log.info("Updated Parent Dir to append ChildrenID of Deleting Dir's ChildrenID, and removed Deleting Dir's ID");
                    } else {
                        EntityConnector conn = (EntityConnector) map.get(mapConn);
                        conn.getRootDirs().addAll(childrenIDs);
                        conn.getRootDirs().remove(targetDir.getId());
                        log.info("Updated RootDirs of Connector to have all ChildrenIDs of Deleting Dir, and removed Deleting Dir's ID");
                    }
                    return map;
                })
                //Save the updated parentDir to db if parentDir is valid, else save connector to db
                .flatMap(map -> {
                    if (map.containsKey(mapParentDir)) {
                        EntityDir parentDir = (EntityDir) map.get(mapParentDir);
                        log.info("Saved updated Parent {} to DB", parentDir);
                        return dirRepo.save(parentDir)
                                .map(savedParentDir -> (EntityDir) map.get(mapTargetDir));
                    } else {
                        EntityConnector connector = (EntityConnector) map.get(mapConn);
                        log.info("Saved updated Connector {} to DB", connector);
                        return connectorRepo.save(connector)
                                .map(savedConn -> (EntityDir) map.get(mapTargetDir));
                    }
                })
                //Delete the targetDir finally
                .flatMap(
                        targetDir ->
                        {
                            log.info("Deleted the Target Dir and return true");
                            //IMPORTANT : Since delete function returns a void it's map will never be triggered.
                            //Use then to send a mono of our choice after the action
                            return dirRepo.deleteById(targetDir.getId())
                                    .then(Mono.just(true));
                        }
                );
    }

    public Mono<EntityDir> getDir(String dirID, String userID) {
        return dirRepo.findById(dirID).switchIfEmpty(Mono.error(new ResponseException("Directory with the given ID not found", 404)))
                .flatMap(entityDir -> {
                    if (!entityDir.getCreatorID().equalsIgnoreCase(userID) && entityDir.getIsPublic().equalsIgnoreCase("false")) {
                        return Mono.error(new ResponseException("No Access to the directory", 401));
                    }
                    return Mono.just(entityDir);
                })
                ;
    }

    /***
     * Returns Dirs that are children of ParentDirID.
     * If ParentDirID is "*" Then it will return the Root Dirs of the userID
     * @param userID UserID
     * @param parentDirID DirID of the parent directory
     * @return List of Directories
     */
    public Flux<EntityDir> getChildrenDirs(String userID, String parentDirID) {
        /*
        1. Check if its has parentID or not
        2. If not we simply get root dir list from connector and get dirs based on the list and return it
        3. If it has root dir we access the dir and then get its children list and return it
         */
        if (parentDirID == null || parentDirID.isEmpty()) {
            return Flux.error(new ResponseException("Parent ID is invalid", 403));
        }

        if (parentDirID.equalsIgnoreCase("*"))
            return connectorRepo.findById(userID)
                    .flatMapMany(conn -> {
                        List<String> dirs = conn.getRootDirs();
                        return Flux.fromIterable(dirs);
                    })
                    .flatMap(dirIDs -> dirRepo.findById(dirIDs))
                    ;

        return dirRepo.findById(parentDirID).switchIfEmpty(Mono.error(new ResponseException("Parent not found", 404)))
                .flatMapMany(rootDir -> {
                    if (!rootDir.getCreatorID().equalsIgnoreCase(userID))
                        return Mono.error(new ResponseException("Creator ID do not match", 401));
                    var dirs = rootDir.getChildren();
                    return Flux.fromIterable(dirs);
                })
                .flatMap(dirId -> dirRepo.findById(dirId));
    }
}
