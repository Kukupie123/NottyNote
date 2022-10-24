package com.advbkm.db.models.entities;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

@AllArgsConstructor
@Getter
@Setter
@Document(collection = "Directories")
public class EntityDir {
    @MongoId
    private String _id;
    private String creatorID;
    private String isPublic;
    private String[] children;
    private String parent;
    private String[] bookmarks;
}

