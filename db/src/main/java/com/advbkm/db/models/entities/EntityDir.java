package com.advbkm.db.models.entities;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.List;

@AllArgsConstructor
@Getter
@Setter
@ToString
@Document(collection = "Directories")
public class EntityDir {
    @MongoId
    private String _id;
    private String creatorID;
    private String isPublic;
    private String name;
    private String parent;
    private List<String> children;
    private List<String> bookmarks;


}

