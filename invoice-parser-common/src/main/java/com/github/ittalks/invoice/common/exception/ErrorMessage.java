package com.github.ittalks.invoice.common.exception;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;
import org.springframework.util.Assert;

/**
 * Created by 刘春龙 on 2017/6/12.
 */
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class ErrorMessage {
    private String errorCode;//错误码
    private String errorMsg;//错误描述

    @JsonIgnore
    private HttpStatus httpStatus;//状态码

    public ErrorMessage() {
    }

    public ErrorMessage(String errorCode, String errorMsg, HttpStatus httpStatus) {
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
        this.httpStatus = httpStatus;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public ErrorMessage setErrorCode(String errorCode) {
        this.errorCode = errorCode;
        return this;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public ErrorMessage setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
        return this;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public ErrorMessage setHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
        return this;
    }

    public static ErrorMessage create() {
        return new ErrorMessage();
    }

    public static ErrorMessage create(String errorCode, String errorMsg, HttpStatus httpStatus) {
        Assert.notNull(errorCode, "errorCode can not be empty");
        Assert.notNull(errorMsg, "errorMsg can not be empty");
        Assert.notNull(httpStatus, "httpStatus can not be empty");
        return new ErrorMessage(errorCode, errorMsg, httpStatus);
    }
}
