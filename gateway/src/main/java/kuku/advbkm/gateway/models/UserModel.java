package kuku.advbkm.gateway.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserModel {
    private String email;
    private String password;
    private String name;
    private String type;
}
