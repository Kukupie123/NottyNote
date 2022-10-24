package kuku.advbkm.gateway.models;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@AllArgsConstructor
@Getter
@Setter
@ToString
public class DirectoryModel {


    private String _id;
    private String creatorID;
    private String isPublic;
    private String name;
    private String parent;
    private List<String> children;
    private List<String> bookmarks;


}
