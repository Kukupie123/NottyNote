package com.advbkm.db.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class TemplateService {

    public Mono<String> createTemplate() {
        //Create a new record in template record
        //In connector of user-template add new item to list
        Mono.empty();
    }


    public Mono<Boolean> deleteTemplate() {
        //get template
        //get bookmarks with same template id using connector template-bookmark collection
        //delete bookmarks, this should also then delete the bookmark id from directory collection
        //delete bookmark from connector user-bookmark
        //when all bookmarks deleted we remove template from connector user-template
        //then we delete the template finally
    }
}
