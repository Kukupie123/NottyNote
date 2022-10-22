package com.advbkm.db.models.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class User {
    private String email;

    private String password;

    private String name;

    private String type;
}
