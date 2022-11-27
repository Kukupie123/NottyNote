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

    /**
     * Create a template
     * @param template template to create
     * @param userID userID of requester
     * @return ID of the newly created template
     */
    public Mono<String> createTemplate(EntityTemplate template, String userID) {
        String mapSavedTemplate = "savedTemplate";
        String mapFoundConnector = "foundConnector";

        //Set CreatorID and bookmarks
        template.setCreatorID(userID);
        template.setBookmarks(new ArrayList<>());


        return repoTemp.save(template)
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
                //Save the updated connector to db, return the templateID
                .flatMap(map -> {
                    EntityConnector conn = (EntityConnector) map.get(mapFoundConnector);
                    String templateID = ((EntityTemplate) map.get(mapSavedTemplate)).getId();
                    return repoConnector.save(conn)
                            .map(updatedConn -> templateID);
                });

    }

    /**
     * Get a template bu it's ID
     * @param templateID ID of the template we want to get
     * @param userID userID of the requester
     * @return Template Entity
     */
    public Mono<EntityTemplate> getTemplate(String templateID, String userID) {
        return repoTemp.findById(templateID).switchIfEmpty(Mono.error(new ResponseException("Template not found", 404)))
                //Validate CreatorID
                .flatMap(template -> {
                    if (!template.getCreatorID().equalsIgnoreCase(userID))
                        return Mono.error(new ResponseException("CreatorID and requester do not match", 401));
                    return Mono.just(template);
                });
    }

    /**
     * Get list of templates created by the user
     * @param userID userID of the requester
     * @return a list of template
     */
    public Flux<EntityTemplate> getTemplatesForUser(String userID) {
        return repoConnector.findById(userID).switchIfEmpty(Mono.error(new ResponseException("User not found", 404)))
                .flatMapMany(entityConnector -> Flux.fromIterable(entityConnector.getTemplates().stream().toList()))
                .flatMap(s -> repoTemp.findById(s));
    }


    /**
     * Delete a template using it's ID
     * @param templateID ID of the template you want to delete
     * @param userID userID of requester
     * @return true if deletion was successful else false
     */
    public Mono<Boolean> deleteTemplate(String templateID, String userID) {

        final String mapTemp = "temp";
        final String mapBookmarks = "bookmarks";
        final String mapDirs = "dirs";
        final String mapConn = "conn";


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
                            .flatMap(s -> repoBookmark.findById(s))
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
                            .flatMap(s -> repoDir.findById(s))
                            //Update the dir bookmark list by removing all the bookmarks
                            .map(entityDir -> {
                                List<String> bookmarks = (List<String>) map.get(mapBookmarks);
                                entityDir.getBookmarks().removeAll(bookmarks);
                                return entityDir;
                            })
                            //Save the updated dir back
                            .flatMap(entityDir -> repoDir.save(entityDir))
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
                //Get the connector
                .flatMap(map -> {
                    EntityTemplate template = (EntityTemplate) map.get(mapTemp);
                    return repoConnector.findById(template.getCreatorID())
                            .map(entityConnector -> {
                                map.put(mapConn, entityConnector);
                                return map;
                            });
                })
                //Update the connector by removing the templateID as well as bookmarks
                .map(map -> {
                    EntityTemplate template = (EntityTemplate) map.get(mapTemp);
                    List<String> bookmarks = (List<String>) map.get(mapBookmarks);
                    EntityConnector connector = (EntityConnector) map.get(mapConn);
                    connector.getBookmarks().removeAll(bookmarks);
                    connector.getTemplates().remove(template.getId());
                    return map;
                })
                //Save the updated map to collection
                .flatMap(map -> {
                    EntityConnector connector = (EntityConnector) map.get(mapConn);
                    return repoConnector.save(connector)
                            .map(entityConnector -> map);
                })
                //Delete the template
                .flatMap(map -> repoTemp.deleteById(templateID)
                        .then(Mono.just(true)))
                ;
    }
}
