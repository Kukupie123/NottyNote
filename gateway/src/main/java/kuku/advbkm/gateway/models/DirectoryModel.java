package kuku.advbkm.gateway.models;


import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class DirectoryModel {


    private String id;
    private String creatorID;
    private String isPublic;
    private String name;
    private String parent;
    private List<String> children;
    private List<String> bookmarks;


}
