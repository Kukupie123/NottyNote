package kuku.advbkm.gateway.models.ReqRespBodies;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RequestUserLogin {
    private String email;
    private String password;
}
