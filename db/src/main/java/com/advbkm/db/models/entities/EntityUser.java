package com.advbkm.db.models.entities;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@AllArgsConstructor
@Getter
@Document(collection = "Users")
public class EntityUser {
    @Id
    private String email;

    private String password;

    private String name;
}
