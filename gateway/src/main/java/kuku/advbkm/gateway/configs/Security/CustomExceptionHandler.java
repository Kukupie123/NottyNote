package kuku.advbkm.gateway.configs.Security;

import kuku.advbkm.gateway.models.ReqResp.ReqResp;
import kuku.advbkm.gateway.models.ResponseExceptionModel;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Log4j2
@RestControllerAdvice
public class CustomExceptionHandler {
    @ExceptionHandler(ResponseExceptionModel.class)
    public ResponseEntity<ReqResp<?>> handleResponseException(ResponseExceptionModel e) {
        log.warn("Response Exception with data {}", e.toString());
        return ResponseEntity.status(e.getStatusCode()).body(new ReqResp<>(null, e.getMessage()));
    }
}