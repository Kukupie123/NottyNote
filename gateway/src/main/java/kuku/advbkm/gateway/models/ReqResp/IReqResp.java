package kuku.advbkm.gateway.models.ReqResp;

public interface IReqResp<T>{
    String getMsg();
    T getData();
}
