package kuku.advbkm.gateway.models;

import lombok.*;

import java.util.HashMap;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class TemplateModel {
    private String name;
    private String creatorID;
    private HashMap<String, TemplateField> struct;
}
