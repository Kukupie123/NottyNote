package models.ReqNRespModel;

public interface IRespModel<T> {

    void setData(T data);

    T getData();

    void setDataAndMsg(T data, String message);

    String getMessage();

    void setMessage(String message);
}
