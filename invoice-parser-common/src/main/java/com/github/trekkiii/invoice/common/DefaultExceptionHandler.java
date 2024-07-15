package com.github.trekkiii.invoice.common;

import com.github.trekkiii.invoice.common.exception.ErrorMessage;
import com.github.trekkiii.invoice.common.exception.NestedException;
import com.github.trekkiii.invoice.common.result.ErrorCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Created by 刘春龙 on 2017/6/6.
 */
@ControllerAdvice
public class DefaultExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = {NestedException.class})
    public ResponseEntity<?> handleNestedException(NestedException e) {
        ErrorMessage eErrorMessage = e.getErrorMessage();
        String reasonPhrase = e.getReasonPhrase();
        if (eErrorMessage != null) {
            return new ResponseEntity<>(eErrorMessage, null, eErrorMessage.getHttpStatus());
        } else if (!StringUtils.isEmpty(reasonPhrase)) {
            ErrorMessage errorMessage = ErrorCode.registerErrorMsg(reasonPhrase);
            return new ResponseEntity<>(errorMessage, null, errorMessage.getHttpStatus());
        } else {
            return new ResponseEntity<>(ErrorCode.ERROR_UNKNOWN, null, ErrorCode.ERROR_UNKNOWN.getHttpStatus());
        }
    }

    @ExceptionHandler(value = {RuntimeException.class})
    public ResponseEntity<?> handleRuntimeException(RuntimeException e) {
        return new ResponseEntity<>(ErrorCode.INTERNAL_SERVER_ERROR, null, ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus());
    }
}
