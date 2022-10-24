package kuku.advbkm.gateway.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class UserModel {
    private String email;
    private String password;
    private String name;
    private String type;
}
