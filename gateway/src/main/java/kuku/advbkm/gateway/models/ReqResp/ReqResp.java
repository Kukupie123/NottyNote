package kuku.advbkm.gateway.models.ReqResp;


import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
public class ReqResp<T> implements IReqResp<T> {

    private T data;
    private String msg;


    @Override
    public String getMsg() {
        return this.msg;
    }

    @Override
    public T getData() {
        return this.data;
    }
}
