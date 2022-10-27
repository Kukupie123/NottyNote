package com.advbkm.db.service;

import com.advbkm.db.models.entities.EntityConnector;
import com.advbkm.db.models.entities.TemplateEntity.EntityTemplate;
import com.advbkm.db.repo.RepoConnector;
import com.advbkm.db.repo.RepoTemplate;
import com.advbkm.db.repo.connectors.RepoConnectorUserToTemp;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

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
        template.setCreatorID(userID);
        //Create a new record in template record
        //In connector of user-template add new item to list

        var finalMono = repoTemp.save(template)
                .flatMap(savedTemplate -> {

                    this.templateID = savedTemplate.getId();


                    //Get connector record or create new one, then add the new template list
                    return repoConnector.findById(savedTemplate.getCreatorID()).defaultIfEmpty(new EntityConnector(savedTemplate.getCreatorID()));
                })
                .flatMap(conn -> {

                    //Add the new template ID to the list and save it
                    conn.getTemplates().add(templateID);

                    return repoConnector.save(conn);

                })
                //After saving we simply return the newly created Template's ID
                .map(e -> this.templateID);

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
