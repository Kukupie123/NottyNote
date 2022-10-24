package kuku.advbkm.gateway.models;


import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class DirectoryModel {
    public DirectoryModel(String name, boolean isPublic, String parentID) {
        this.name = name;
        this.isPublic = isPublic;
        this.parent = parentID;
    }

    private String id;
    private String name;
    private boolean isPublic;
    private String parent;
    private String[] children;
    private String[] bookmarks;

}
