package kuku.advbkm.gateway.models.ReqResp;


import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
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
