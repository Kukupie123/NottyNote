package kuku.advbkm.gateway.models;

import lombok.*;

import java.util.HashMap;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class BookmarkModel {
    private String id;
    private String creatorID;
    private String templateID;
    private String dirID;
    private String name;
    private boolean isPublic;
    private HashMap<String, Object> data;
}
