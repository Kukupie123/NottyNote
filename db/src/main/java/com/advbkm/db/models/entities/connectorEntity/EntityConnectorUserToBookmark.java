package com.advbkm.db.models.entities.connectorEntity;


import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Document(collection = "Connector_User2Bookmark")
public class EntityConnectorUserToBookmark {
    @MongoId
    private String creatorID;
    private List<String> bookmarks;
}
