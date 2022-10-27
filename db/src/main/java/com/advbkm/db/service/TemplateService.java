package com.advbkm.db.service;

import com.advbkm.db.models.entities.TemplateEntity.EntityTemplate;
import com.advbkm.db.models.entities.connectorEntity.EntityConnectorUserToTemp;
import com.advbkm.db.repo.RepoConnectorUserToTemp;
import com.advbkm.db.repo.RepoTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Service
public class TemplateService {


    private final RepoTemplate repoTemp;
    private final RepoConnectorUserToTemp repoConnectorUserToTemp;


    private String templateID; //Used to save templateID when creating template as we lose templateID information once we transform it

    public TemplateService(RepoTemplate repoTemp, RepoConnectorUserToTemp repoConnectorUserToTemp) {
        this.repoTemp = repoTemp;
        this.repoConnectorUserToTemp = repoConnectorUserToTemp;
    }

    public Mono<String> createTemplate(EntityTemplate template, String userID) {
        template.setCreatorID(userID);
        //Create a new record in template record
        //In connector of user-template add new item to list

        var finalMono = repoTemp.save(template)
                //Using the savedTempID try to get existing or newly created EntityConnectorUsrToTemp
                .flatMap(savedTemp -> {

                    //Saving templateID to a class member because once transformed this information will be gone
                    this.templateID = savedTemp.getId();
                    String savedTempID = savedTemp.getId();

                    List<String> stringList = new ArrayList<>();
                    stringList.add(savedTempID);


                    //Try to get saved list
                    return repoConnectorUserToTemp.findById(savedTemp.getCreatorID())
                            //If prev record not found we will create new entity
                            .defaultIfEmpty(new EntityConnectorUserToTemp(savedTemp.getCreatorID(), stringList))
                            .map(foundUser2Temp -> {
                                //To update if record already exists, we need to add the new templateID
                                if (foundUser2Temp.getTemplateIDs().contains(savedTempID) == false)
                                    foundUser2Temp.getTemplateIDs().add(savedTempID);

                                return foundUser2Temp;
                            });
                })
                //With the entityConnectorUserToTemp object, save it back to the collection
                .flatMap(updatedUser2Temp -> {
                    return repoConnectorUserToTemp.save(updatedUser2Temp)
                            .map(savedUser2Temp -> this.templateID);
                });

        return finalMono.onErrorResume(err -> Mono.error(new Exception(err.getMessage())));

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
