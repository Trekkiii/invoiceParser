package com.github.fnpac.invoice.common.exception;

/**
 * Created by 刘春龙 on 2017/6/6.
 */
public class NestedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private ErrorMessage errorMessage;
    private String reasonPhrase;

    public NestedException() {
        super();
    }

    public NestedException(String message) {
        super(message);
        this.reasonPhrase = message;
    }

    public NestedException(String message, Throwable cause) {
        super(message, cause);
        this.reasonPhrase = message;
    }

    public NestedException(Throwable cause) {
        super(cause);
    }

    public NestedException(ErrorMessage errorMessage) {
        super(errorMessage.getErrorMsg());
        this.errorMessage = errorMessage;
    }

    public NestedException(ErrorMessage errorMessage, String message) {
        super(message);
        this.errorMessage = errorMessage;
    }

    public NestedException(ErrorMessage errorMessage, String message, Throwable cause) {
        super(message, cause);
        this.errorMessage = errorMessage;
    }

    public NestedException(ErrorMessage errorMessage, Throwable cause) {
        super(cause);
        this.errorMessage = errorMessage;
    }

    public ErrorMessage getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(ErrorMessage errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getReasonPhrase() {
        return reasonPhrase;
    }

    public void setReasonPhrase(String reasonPhrase) {
        this.reasonPhrase = reasonPhrase;
    }

    public Throwable getRootCause() {
        Throwable t = this;
        while (true) {
            Throwable cause = t.getCause();
            if (cause != null) {
                t = cause;
            } else {
                break;
            }
        }
        return t;
    }

    @Override
    public synchronized Throwable getCause() {
        return super.getCause();
    }

    @Override
    public void printStackTrace() {
        super.printStackTrace();
    }
}
