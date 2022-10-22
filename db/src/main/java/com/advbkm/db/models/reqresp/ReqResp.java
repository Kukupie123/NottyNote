package com.advbkm.db.models.reqresp;


import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ReqResp<T> implements IReqResp<T> {

    private T data;
    private String msg;

    @Override
    public T getData() {
        return this.data;
    }

    @Override
    public String getMsg() {
        return this.msg;
    }
}
