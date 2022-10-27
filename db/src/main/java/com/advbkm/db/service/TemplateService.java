package com.advbkm.db.service;

import com.advbkm.db.models.entities.EntityConnector;
import com.advbkm.db.models.entities.TemplateEntity.EntityTemplate;
import com.advbkm.db.repo.RepoConnector;
import com.advbkm.db.repo.RepoTemplate;
import com.advbkm.db.repo.connectors.RepoConnectorUserToTemp;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Service
public class TemplateService {


    private final RepoTemplate repoTemp;
    private final RepoConnectorUserToTemp repoConnectorUserToTemp;
    private final RepoConnector repoConnector;


    private String templateID; //Used to save templateID when creating template as we lose templateID information once we transform it

    public TemplateService(RepoTemplate repoTemp, RepoConnectorUserToTemp repoConnectorUserToTemp, RepoConnector repoConnector) {
        this.repoTemp = repoTemp;
        this.repoConnectorUserToTemp = repoConnectorUserToTemp;
        this.repoConnector = repoConnector;
    }

    public Mono<String> createTemplate(EntityTemplate template, String userID) {

        //Names of map, map that we will create inside mono to save object that will otherwise be lost during mono/flux transformation
        String mapSavedTemplate = "savedTemplate";
        String mapFoundConnector = "foundConnector";

        template.setCreatorID(userID);

        /*
        1. Save the template
        2. Get/Create EntityConnector
        3. Add the templateID to Connector we found
        4. Save the updated Connector
         */

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


    public Mono<Boolean> deleteTemplate() {
        //get template
        //get bookmarks with same template id using connector template-bookmark collection
        //delete bookmarks, this should also then delete the bookmark id from directory collection
        //delete bookmark from connector user-bookmark
        //when all bookmarks deleted we remove template from connector user-template
        //then we delete the template finally
        return Mono.just(false);
    }
}
