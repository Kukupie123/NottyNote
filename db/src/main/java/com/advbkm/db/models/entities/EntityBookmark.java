package com.advbkm.db.models.entities;


import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.HashMap;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Document(collection = "Bookmark")
public class EntityBookmark {
    @MongoId
    private String id;
    private String creatorID;
    private String templateID;
    private String name;
    private boolean isPublic;
    private HashMap<String, Object> data;
}
