package com.advbkm.db.service;


import com.advbkm.db.models.entities.EntityBookmark;
import com.advbkm.db.models.entities.EntityConnector;
import com.advbkm.db.models.entities.EntityDir;
import com.advbkm.db.models.entities.TemplateEntity.EntityTemplate;
import com.advbkm.db.models.entities.TemplateEntity.LayoutFieldType;
import com.advbkm.db.models.exception.ResponseException;
import com.advbkm.db.repo.RepoBookmark;
import com.advbkm.db.repo.RepoConnector;
import com.advbkm.db.repo.RepoDir;
import com.advbkm.db.repo.RepoTemplate;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
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
        6. Add the bookmarkID to template's bookmark list
        6. Adding bookmarkID to dir bookmarks list
        7. Adding the bookmarkID to connector
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


        return repoTemplate.findById(bookmark.getTemplateID()).switchIfEmpty(Mono.error(new ResponseException("Template with specified ID not found", 404))) //Get the template
                //validate the template returned
                .flatMap(foundTemplate -> {
                    if (!foundTemplate.getCreatorID().equalsIgnoreCase(userID)) {
                        return Mono.error(new ResponseException("Template Creator ID do not match userID extracted from JWT Token", 401));
                    }
                    return Mono.just(foundTemplate);
                })
                //Validate if the bookmark has all the mandatory fields supplied to it
                .flatMap(foundTemp -> {
                    try {
                        foundTemp.getStruct().forEach((fieldName, fieldStruct) -> {
                            //Check if bookmark contains key and fieldType and the data match
                            if (!bookmark.getData().containsKey(fieldName)) {
                                //If key is not present, it has to be optional
                                if (!fieldStruct.isOptional())
                                    throw new ResponseException("Bookmark is missing mandatory field ( " + fieldName + " )", HttpStatus.BAD_REQUEST.value());
                            }
                        });
                    } catch (ResponseException e) {
                        return Mono.error(e);
                    }
                    return Mono.just(foundTemp);
                })
                //Validate if the bookmark's data are the same as the template's Structs. Also check if the data match the fieldType
                .map(foundTemp -> {
                    try {
                        bookmark.getData().forEach((fieldName, fieldData) -> {
                            //Does the bookmark contain only the fields that the template has
                            if (foundTemp.getStruct().containsKey(fieldName)) {
                                //The template has the field. Check if the data matches the fieldType
                                LayoutFieldType fieldType = foundTemp.getStruct().get(fieldName).getFieldType();
                                switch (fieldType) {
                                    case TEXT, LINK -> {
                                        var castedData = (String) fieldData;
                                    }
                                    case IMAGE -> {
                                        //IDK How to validate this yet
                                    }
                                    case LIST_TEXT, LIST_LINK -> {
                                        var castedData = (List<String>) fieldData;
                                    }
                                    case LIST_IMAGE -> {
                                        //IDK How to validate this yet
                                        var castedData = (List<Object>) fieldData;
                                    }
                                }
                            } else {
                                //The template is missing the bookmark's field
                                throw new ResponseException("Bookmark has field that the template doesn't.", 403);
                            }
                        });
                        return foundTemp;
                    } catch (Exception e) {
                        throw new ResponseException(e.getMessage(), 500);
                    }
                })
                //Get the directory, return a map which will store the argument object to avoid losing it during transformation using map/flatmap
                .flatMap(foundTemp -> {
                    log.info("FINDING DIR ID {}", bookmark.getDirID());

                    return repoDir.findById(bookmark.getDirID()).switchIfEmpty(Mono.error(new ResponseException("Directory not found", 404)))
                            .map(foundDir -> {
                                Map<String, Object> map = new HashMap<>();
                                map.put(mapFoundTemp, foundTemp);
                                map.put(mapFoundDir, foundDir);
                                return map;
                            });
                })
                //Validate foundDir
                .flatMap(map -> {
                    EntityDir foundDir = (EntityDir) map.get(mapFoundDir);
                    log.info(foundDir);
                    if (!foundDir.getCreatorID().equalsIgnoreCase(userID)) {
                        return Mono.error(new ResponseException("Directory Creator ID do not match userID extracted from JWT Token", 401));
                    }
                    return Mono.just(map);
                })
                //Template and Dir Valid, Now save the bookmark, put it in map and return the map
                .flatMap(map -> repoBookmark.save(bookmark)
                        .map(savedBKM -> {
                            map.put(mapSavedBkm, savedBKM);
                            return map;
                        }))
                // Update the bookmarks list of the directory
                .map(map -> {
                    EntityDir foundDir = (EntityDir) map.get(mapFoundDir);
                    EntityBookmark savedBKM = (EntityBookmark) map.get(mapSavedBkm);
                    foundDir.getBookmarks().add(savedBKM.getId());
                    return map;
                })
                // Saving the updated directory
                .flatMap(map -> {

                    EntityDir foundDir = (EntityDir) map.get(mapFoundDir);
                    return repoDir.save(foundDir)
                            .map(updatedDir -> {
                                map.put(mapFoundDir, updatedDir);
                                return map;
                            });
                })
                // Update the template by adding the newly created bookmark
                .map(map -> {
                    EntityBookmark savedBkm = (EntityBookmark) map.get(mapSavedBkm);
                    EntityTemplate template = (EntityTemplate) map.get(mapFoundTemp);
                    template.getBookmarks().add(savedBkm.getId());
                    return map;
                })
                // Save the updated template
                .flatMap(map -> {
                    EntityTemplate template = (EntityTemplate) map.get(mapFoundTemp);
                    return repoTemplate.save(template)
                            .map(
                                    template1 -> map
                            );
                })
                //Get/Create the connector, save it to map and return the map
                .flatMap(map -> repoConnector.findById(userID).defaultIfEmpty(new EntityConnector(userID))
                        .map(foundConn -> {
                            map.put(mapFoundConn, foundConn);
                            return map;
                        }))
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
                            .map(updatedConn -> ((EntityBookmark) map.get(mapSavedBkm)).getId());
                })
                //Add the bookmarkID to bookmarks list of template
                ;


    }


    public Mono<Boolean> deleteBookmark(String id, String userID) {
        /*
        1. Get the bookmark
        2. Get template and validate
        2. Validate and Check if creatorID and userId match among all them
        3. Remove the bookmarkID from template
        4. Save the updated template
        3. Get the directory based on ID
        4. Delete the bookmark from dir's bookmark list
        5. Save the updated dir
        6. Get the connector
        7. Update the connector bookmark list
        8. Delete the bookmark from bookmark collection
        DONE
         */

        String mapFoundBKM = "foundBKM";
        String mapFoundDir = "foundDir";
        String mapFoundConn = "foundConn";
        String mapFoundTemp = "foundTemp";

        return repoBookmark.findById(id).switchIfEmpty(Mono.error(new ResponseException("Bookmark not found", 404)))
                //Get the template
                .flatMap(bookmark -> repoTemplate.findById(bookmark.getTemplateID()).switchIfEmpty(Mono.error(new ResponseException("Template not found", 404)))
                        .map(template -> {
                            Map<String, Object> map = new HashMap<>();
                            map.put(mapFoundTemp, template);
                            map.put(mapFoundBKM, bookmark);
                            return map;
                        }))
                //Validate the creatorId and userID among all of these
                .flatMap(map -> {
                    EntityBookmark bookmark = (EntityBookmark) map.get(mapFoundBKM);
                    EntityTemplate template = (EntityTemplate) map.get(mapFoundTemp);
                    if (!bookmark.getCreatorID().equalsIgnoreCase(userID) && !bookmark.getCreatorID().equalsIgnoreCase(template.getCreatorID())) {
                        return Mono.error(new ResponseException(String.format("Creator ID of the bookmark %s and userID  %s do not match. CreatorID of bookmark ", bookmark.getCreatorID(), userID), 401));
                    }
                    return Mono.just(map);
                })
                //Remove bookmarkID from template
                .map(map -> {
                    EntityTemplate template = (EntityTemplate) map.get(mapFoundTemp);
                    EntityBookmark bookmark = (EntityBookmark) map.get(mapFoundBKM);
                    template.getBookmarks().remove(bookmark.getId());
                    return map;
                })
                //Save the updated template
                .flatMap(map -> {
                    EntityTemplate template = (EntityTemplate) map.get(mapFoundTemp);
                    return repoTemplate.save(template)
                            .map(template1 -> map);
                })
                //Get the directory based on dirID
                .flatMap(map -> {
                    EntityBookmark bookmark = (EntityBookmark) map.get(mapFoundBKM);
                    return repoDir.findById(bookmark.getDirID())
                            .switchIfEmpty(Mono.error(new ResponseException("Directory not found", 404)))
                            .map(foundDir -> {
                                map.put(mapFoundDir, foundDir);
                                map.put(mapFoundBKM, bookmark);
                                return map;
                            });
                })
                //Get the Connector based on creatorID
                .flatMap(map -> {
                    EntityBookmark bookmark = (EntityBookmark) map.get(mapFoundBKM);
                    return repoConnector.findById(bookmark.getCreatorID())
                            .switchIfEmpty(Mono.error(new ResponseException("Connector not found", 404)))
                            .map(entityConnector -> {
                                map.put(mapFoundConn, entityConnector);
                                return map;
                            });
                })
                //Update the dir's bookmarks list
                .map(map -> {

                    EntityDir dir = (EntityDir) map.get(mapFoundDir);
                    EntityBookmark bookmark = (EntityBookmark) map.get(mapFoundBKM);
                    dir.getBookmarks().remove(bookmark.getId());
                    return map;
                })
                //Save the newly updated Dir to collection
                .flatMap(map -> {

                    EntityDir dir = (EntityDir) map.get(mapFoundDir);
                    return repoDir.save(dir)
                            .map(entityDir -> map);
                })
                // Update Connector
                .map(map -> {
                    EntityConnector connector = (EntityConnector) map.get(mapFoundConn);
                    EntityBookmark bookmark = (EntityBookmark) map.get(mapFoundBKM);
                    connector.getBookmarks().remove(bookmark.getId());
                    return map;
                })
                //Save the newly updated conn
                .flatMap(map -> {
                    EntityConnector connector = (EntityConnector) map.get(mapFoundConn);
                    return repoConnector.save(connector)
                            .map(c -> map);
                })
                //Delete the bookmark now
                .flatMap(map -> repoBookmark.deleteById(id)
                        .then(Mono.just(true)))
                ;
    }

    public Mono<EntityBookmark> getBookmark(String id, String userID) {
        return repoBookmark.findById(id).switchIfEmpty(Mono.error(new ResponseException("Bookmark not found", 404)))
                .flatMap(bookmark -> {
                    if (!bookmark.getCreatorID().equalsIgnoreCase(userID) && !bookmark.isPublic())
                        return Mono.error(new ResponseException("Access violation", 401));
                    return Mono.just(bookmark);
                });
    }

    public Flux<EntityBookmark> getBookmarksFromDir(String dirID, String userID) {
        /*
        1. Get dir and validate the creator ID
        2. Get the bookmarks ID list from it
        3. Iterate the bookmark IDs and return them
         */

        return repoDir.findById(dirID).switchIfEmpty(Mono.error(new ResponseException("Directory not found", 404)))
                //Validate creatorID
                .flatMap(dir -> {
                    if (!dir.getCreatorID().equalsIgnoreCase(userID))
                        return Mono.error(new ResponseException("No access to directory", 401));
                    return Mono.just(dir);
                })
                //Get bookmarks list from dir
                .flatMapMany(dir -> {
                    List<String> bookmarkIDs = dir.getBookmarks();
                    return Flux.fromIterable(bookmarkIDs);
                })
                //Get bookmarks
                .flatMap(bookmarkID -> repoBookmark.findById(bookmarkID));
    }
}

