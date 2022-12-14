package com.advbkm.db.models.entities;


import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Document(collection = "Users")
public class EntityUser {
    @MongoId(targetType = FieldType.STRING,value = FieldType.STRING)
    private String email;

    private String password;

    private String name;

    private String type;
}
