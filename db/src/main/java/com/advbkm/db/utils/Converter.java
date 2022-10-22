package com.advbkm.db.utils;

import com.advbkm.db.models.entities.EntityUser;
import com.advbkm.db.models.entities.User;

public class Converter {
    private Converter() {
    }

    public static User entityUser2User(EntityUser entityUser) {
        return new User(entityUser.getEmail(), entityUser.getPassword(), entityUser.getName(), entityUser.getType());
    }
}
