package com.advbkm.db.models.entities;


import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Document(collection = "Connector")
public class EntityConnector {

    public EntityConnector(String userID) {
        this.userID = userID;
        this.templates = new ArrayList<>();
        this.bookmarks = new ArrayList<>();
        this.rootDirs = new ArrayList<>();


    }

    @MongoId
    private String userID;

    private List<String> templates;
    private List<String> bookmarks;
    private List<String> rootDirs;
}
