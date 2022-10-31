package com.advbkm.db.service;

import com.advbkm.db.models.entities.EntityConnector;
import com.advbkm.db.models.entities.TemplateEntity.EntityTemplate;
import com.advbkm.db.models.exception.ResponseException;
import com.advbkm.db.repo.RepoBookmark;
import com.advbkm.db.repo.RepoConnector;
import com.advbkm.db.repo.RepoDir;
import com.advbkm.db.repo.RepoTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TemplateService {


    private final RepoTemplate repoTemp;
    private final RepoConnector repoConnector;
    private final RepoBookmark repoBookmark;
    private final RepoDir repoDir;


    public TemplateService(RepoTemplate repoTemp, RepoConnector repoConnector, RepoBookmark repoBookmark, RepoDir repoDir) {
        this.repoTemp = repoTemp;
        this.repoConnector = repoConnector;
        this.repoBookmark = repoBookmark;
        this.repoDir = repoDir;
    }

    public Mono<String> createTemplate(EntityTemplate template, String userID) {
         /*
        1. Save the template
        2. Get/Create EntityConnector
        3. Add the templateID to Connector we found
        4. Save the updated Connector
         */

        //Names of map, map that we will create inside mono to save object that will otherwise be lost during mono/flux transformation
        String mapSavedTemplate = "savedTemplate";
        String mapFoundConnector = "foundConnector";

        template.setCreatorID(userID);
        template.setBookmarks(new ArrayList<>());


        var finalMono = repoTemp.save(template)
                //Get Connector record or create a new one, then return a map
                .flatMap(savedTemplate -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put(mapSavedTemplate, savedTemplate);
                    return repoConnector.findById(savedTemplate.getCreatorID()).defaultIfEmpty(new EntityConnector(savedTemplate.getCreatorID()))
                            //Add the connector we found/created
                            .map(foundConnector -> {
                                map.put(mapFoundConnector, foundConnector);
                                return map;
                            });
                })
                // Update the connector
                .map(map -> {
                    EntityConnector conn = (EntityConnector) map.get(mapFoundConnector);
                    String templateID = ((EntityTemplate) map.get(mapSavedTemplate)).getId();
                    //Add the new template ID to the list and save it
                    conn.getTemplates().add(templateID);
                    return map;

                })
                //Save the updated connector, return the templateID
                .flatMap(map -> {
                    EntityConnector conn = (EntityConnector) map.get(mapFoundConnector);
                    String templateID = ((EntityTemplate) map.get(mapSavedTemplate)).getId();
                    return repoConnector.save(conn)
                            .map(updatedConn -> templateID);
                });
        return finalMono;

    }


    public Mono<Boolean> deleteTemplate(String templateID, String userID) {
        /*
        1. Get the templateID and validate if it's correct based on UserID
        2. Get bookmarks list from temp and store in a list (MAP) as well as create a new list for storing dirIDs
        3. Convert the bookmarks list into flux -> access each bookmark and store dir in map, collect the whole flux as mono list and return back the map
        4. Now we have a list of dirIDs to work with
        5. Collect the dirIDs into flux -> access each dir and remove the bookmarks, collect the whole flux as mono list and return map
        6. Delete all bookmarks
        7. Delete template
         */


        final String mapTemp = "temp";
        final String mapBookmarks = "bookmarks";
        final String mapDirs = "dirs";


        return repoTemp.findById(templateID)
                .switchIfEmpty(Mono.error(new ResponseException("Template not found", 404)))
                //Validate if the userID match the
                .flatMap(template -> {
                    if (!template.getCreatorID().equalsIgnoreCase(userID)) {
                        return Mono.error(new ResponseException("UserID do not match", 404));
                    }
                    return Mono.just(template);
                })
                //Store the bookmarks, create new list for storing dirs
                .map(template -> {
                    var bookmarks = template.getBookmarks();
                    Map<String, Object> map = new HashMap<>();
                    map.put(mapBookmarks, bookmarks);
                    map.put(mapTemp, template);
                    map.put(mapDirs, new ArrayList<String>());
                    return map;
                })
                //Convert the bookmark into a flux, access each bookmark, store DirID, collect the whole flux operation result into a mono, return map
                .flatMap(map -> {
                    List<String> bookmarks = (List<String>) map.get(mapBookmarks);
                    return Flux.fromIterable(bookmarks)
                            //Get each bookmark
                            .flatMap(s -> {
                                return repoBookmark.findById(s);
                            })
                            //Store the dirID in map
                            .map(bookmark -> {
                                List<String> dirs = (List<String>) map.get(mapDirs);
                                dirs.add(bookmark.getDirID());
                                return bookmark;
                            })
                            //Collect all flux operation result into one
                            .collectList()
                            //return back the map
                            .map(entityBookmarks -> map);
                })
                //Now we stored all the dirIDs, convert them into flux, update the bookmarks by removing all bookmarksID stored in map, save the updated dirs, collect the whole flux operation into a mono, return map
                .flatMap(map -> {
                    List<String> dirs = (List<String>) map.get(mapDirs);
                    return Flux.fromIterable(dirs)
                            //Get each dir
                            .flatMap(s -> {
                                return repoDir.findById(s);
                            })
                            //Update the dir bookmark list by removing all the bookmarks
                            .map(entityDir -> {
                                List<String> bookmarks = (List<String>) map.get(mapBookmarks);
                                entityDir.getBookmarks().removeAll(bookmarks);
                                return entityDir;
                            })
                            //Save the updated dir back
                            .flatMap(entityDir -> {
                                return repoDir.save(entityDir);
                            })
                            //Collect all flux operation into a mono
                            .collectList()
                            //return back the map
                            .map(entityDirs -> map)
                            ;
                })
                //Delete all bookmarks
                .flatMap(map -> {
                    List<String> bookmarks = (List<String>) map.get(mapBookmarks);
                    return repoBookmark.deleteAllById(bookmarks)
                            .then(Mono.just(map));

                })
                //Delete the template
                .flatMap(map -> {
                    return repoTemp.deleteById(templateID)
                            .then(Mono.just(true));
                })
                ;
    }
}
