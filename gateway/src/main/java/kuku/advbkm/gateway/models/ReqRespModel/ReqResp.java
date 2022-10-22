package kuku.advbkm.gateway.models.ReqRespModel;


import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class ReqResp<T> implements IReqRespModel<T> {

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
