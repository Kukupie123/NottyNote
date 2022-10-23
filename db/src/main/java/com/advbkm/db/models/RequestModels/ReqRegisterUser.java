package com.advbkm.db.models.RequestModels;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@Setter
@ToString
public class ReqRegisterUser {

    private String email;
    private String password;
    private String name;
    private String type;
}
