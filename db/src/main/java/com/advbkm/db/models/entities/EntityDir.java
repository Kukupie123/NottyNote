package com.advbkm.db.models.entities;


import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
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

