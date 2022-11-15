package com.advbkm.db.models.entities.TemplateEntity;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class TemplateField {
    private LayoutFieldType fieldType;
    private boolean isOptional;
}
