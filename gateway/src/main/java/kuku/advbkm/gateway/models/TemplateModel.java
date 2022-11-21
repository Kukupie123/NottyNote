package kuku.advbkm.gateway.models;

import lombok.*;

import java.util.HashMap;
import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class TemplateModel {
    private String id;
    private String name;
    private String creatorID;
    private List<String> bookmarks;
    private HashMap<String, TemplateField> struct; //name of field  : fieldStruct
}
