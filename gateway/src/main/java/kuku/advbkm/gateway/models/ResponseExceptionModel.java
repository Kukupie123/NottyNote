package kuku.advbkm.gateway.models;


import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ResponseExceptionModel extends Throwable {
    private final String msg;
    private final int statusCode;

    @Override
    public String getMessage() {
        return this.msg;
    }

    public int getStatusCode() {
        return this.statusCode;
    }
}
