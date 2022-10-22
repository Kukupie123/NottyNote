package com.advbkm.db.models.RequestModels;


import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ReqRegisterUser {

    private String email;
    private String password;
    private String name;
}
