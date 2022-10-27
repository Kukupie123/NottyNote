package com.advbkm.db.models.entities.connectorEntity;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Document(collection = "Connector_User2Templates")
public class EntityConnectorUserToTemp {
    @MongoId
    private String creatorID;
    private List<String> templateIDs;
}
