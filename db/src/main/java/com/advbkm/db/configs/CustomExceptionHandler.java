package com.advbkm.db.configs;

import com.advbkm.db.models.exception.ResponseException;
import com.advbkm.db.models.reqresp.ReqResp;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Log4j2
@RestControllerAdvice
public class CustomExceptionHandler {
    @ExceptionHandler(ResponseException.class)
    public ResponseEntity<ReqResp<?>> handleRespException(ResponseException e) {
        log.warn("Response Exception handled by customExceptionHandler with body {}",e.toString());
        return ResponseEntity.status(e.getStatusCode()).body(new ReqResp<>(null, e.getMessage()));
    }
}
