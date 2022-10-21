package kuku.advbkm.gateway.models.ReqRespModel;


import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ReqRespModel<T> implements IReqRespModel<T> {
    private String msg;
    private T data;

    @Override
    public String getMsg() {
        return this.msg;
    }

    @Override
    public T getData() {
        return this.data;
    }
}
