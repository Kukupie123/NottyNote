package com.advbkm.db.models.entities.TemplateEntity;


import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.HashMap;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Document(collection = "BookmarkTemplate")
public class EntityTemplate {
    @MongoId
    private String id;
    private String name;
    private String creatorID;
    private List<String> bookmarks;
    private HashMap<String, TemplateField> struct; //name of field  : fieldStruct
}
