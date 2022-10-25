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
@Document(collection = "Connector_User2RootDir")
public class EntityConnectorUserToDir {
    @MongoId
    private String userID;
    private List<String> dirs;
}
