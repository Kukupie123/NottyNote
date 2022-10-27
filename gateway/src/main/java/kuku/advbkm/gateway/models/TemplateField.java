package kuku.advbkm.gateway.models;


import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class TemplateField {
private String fieldType;
private boolean isOptional;
}
