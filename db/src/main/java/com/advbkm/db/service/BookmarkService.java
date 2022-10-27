package com.advbkm.db.service;


import com.advbkm.db.models.entities.EntityBookmark;
import com.advbkm.db.models.entities.EntityConnector;
import com.advbkm.db.models.entities.EntityDir;
import com.advbkm.db.models.entities.TemplateEntity.EntityTemplate;
import com.advbkm.db.models.exception.ResponseException;
import com.advbkm.db.repo.RepoBookmark;
import com.advbkm.db.repo.RepoConnector;
import com.advbkm.db.repo.RepoDir;
import com.advbkm.db.repo.RepoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Service
public class BookmarkService {

    private final RepoTemplate repoTemplate;
    private final RepoConnector repoConnector;
    private final RepoBookmark repoBookmark;
    private final RepoDir repoDir;

    public BookmarkService(RepoTemplate repoTemplate, RepoConnector repoConnector, RepoBookmark repoBookmark, RepoDir repoDir) {
        this.repoTemplate = repoTemplate;
        this.repoConnector = repoConnector;
        this.repoBookmark = repoBookmark;
        this.repoDir = repoDir;
    }

    public Mono<String> createBookmark(EntityBookmark bookmark, String userID) {
        /*
        1. Basic validations and setting values
        2. Validating if templateID is valid
        3. Validating if dirID is valid
        4. Validating if mandatory keys of templateID match up with the keys supplied
        5. Saving the bookmark.
        6. Adding the bookmarkID to connector
         */

        String mapFoundTemp = "foundTemp";
        String mapFoundDir = "foundDir";
        String mapSavedBkm = "savedBKM";
        String mapFoundConn = "foundConn";

        bookmark.setCreatorID(userID); //Set up the creatorID
        bookmark.setId(null);
        //Basic validation
        if (bookmark.getName() == null || bookmark.getName().isEmpty() || bookmark.getTemplateID() == null || bookmark.getTemplateID().isEmpty()) {
            return Mono.error(new ResponseException("Fields are not valid", HttpStatus.BAD_REQUEST.value()));
        }


        return repoTemplate.findById(bookmark.getTemplateID()).defaultIfEmpty(new EntityTemplate()) //Get the template
                //validate the template returned
                .flatMap(foundTemplate -> {

                    if (foundTemplate.getId() == null || foundTemplate.getId().isEmpty()) {
                        return Mono.error(new ResponseException("Template wth ID " + bookmark.getTemplateID() + " NOT FOUND", 404));
                    }
                    return Mono.just(foundTemplate);
                })
                //Validate if the bookmark has all the mandatory fields supplied to it
                .flatMap(foundTemp -> {

                    try {
                        foundTemp.getStruct().forEach((key, value) -> {

                            //Check if bookmark contains key
                            if (!bookmark.getData().containsKey(key)) {

                                //If key is not present, it has to be optional
                                if (!value.isOptional())
                                    throw new ResponseException("Bookmark is missing mandatory field ( " + key + " )", HttpStatus.BAD_REQUEST.value());
                            }
                        });
                    } catch (ResponseException e) {
                        return Mono.error(e);
                    }

                    return Mono.just(foundTemp);


                })
                //Get the directory, return a map which will store the argument object to avoid losing it during transformation using map/flatmap
                .flatMap(foundTemp -> {

                    Map<String, Object> map = new HashMap<>();
                    map.put(mapFoundTemp, foundTemp);

                    return repoDir.findById(bookmark.getDirID())
                            .map(foundDir -> {
                                map.put(mapFoundDir, foundDir);
                                return map;
                            });
                })
                //Validate foundDir
                .flatMap(map -> {

                    EntityDir foundDir = (EntityDir) map.get(mapFoundDir);

                    if (foundDir.get_id() == null || foundDir.get_id().isEmpty()) {
                        Mono.error(new ResponseException("Directory with the given ID not found.", 404));
                    }
                    return Mono.just(map);
                })
                //Template and Dir Valid, Now save the bookmark, put it in map and return the map
                .flatMap(map -> {

                    return repoBookmark.save(bookmark)
                            .map(savedBKM -> {

                                map.put(mapSavedBkm, savedBKM);
                                return map;
                            });
                })
                //Get/Create the connector, save it to map and return the map
                .flatMap(map -> {

                    return repoConnector.findById(userID).defaultIfEmpty(new EntityConnector(userID))
                            .map(foundConn -> {
                                map.put(mapFoundConn, foundConn);
                                return map;
                            });
                })
                //Add the bookmark ID to the bookmarks list of connector
                .map(map -> {

                    EntityConnector foundConn = (EntityConnector) map.get(mapFoundConn);
                    foundConn.getBookmarks().add(((EntityBookmark) map.get(mapSavedBkm)).getId());

                    return map;
                })
                //Save the newly updated connector, and return back the id of the created bookmark
                .flatMap(map -> {

                    EntityConnector connector = (EntityConnector) map.get(mapFoundConn);
                    return repoConnector.save(connector)
                            .map(updatedConn -> {
                                return ((EntityBookmark) map.get(mapSavedBkm)).getId();
                            });
                });


    }

}

//TODO: Combine all connector into one collection as they all have userID as their PK