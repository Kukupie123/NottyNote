package kuku.advbkm.gateway.models.ReqRespBodies;


import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class RequestDirCreate {

    private String name;
    private boolean isPublic;
    private String parent;
}
