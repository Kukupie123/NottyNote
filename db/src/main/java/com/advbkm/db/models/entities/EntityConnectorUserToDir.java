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
@Document(collection = "Connector_User2RootDir")
public class EntityConnectorUserToDir {
    @MongoId
    private String userID;
    private List<String> dirs;
}
