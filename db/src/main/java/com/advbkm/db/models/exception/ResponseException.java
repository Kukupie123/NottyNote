package com.advbkm.db.models.exception;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class ResponseException extends RuntimeException {
    private String msg;
    private int statusCode;

    @Override
    public String getMessage() {
        return this.msg;
    }


}
