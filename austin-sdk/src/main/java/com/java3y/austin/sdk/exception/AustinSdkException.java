package com.java3y.austin.sdk.exception;

/**
 * Austin SDK 异常类
 *
 * @author 3y
 */
public class AustinSdkException extends RuntimeException {

    private String code;

    public AustinSdkException(String message) {
        super(message);
    }

    public AustinSdkException(String code, String message) {
        super(message);
        this.code = code;
    }

    public AustinSdkException(String message, Throwable cause) {
        super(message, cause);
    }

    public AustinSdkException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
