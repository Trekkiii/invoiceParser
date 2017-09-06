package com.github.ittalks.invoice.exceptions;

/**
 * Created by 刘春龙 on 2017/9/6.
 */
public class InvalidQRContentException extends RuntimeException {

    public InvalidQRContentException(String message) {
        super(message);
    }

    public InvalidQRContentException(String message, Throwable cause) {
        super(message, cause);
    }
}
