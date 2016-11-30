package com.main;

/**
 * Created by zongzesheng on 11/30/16.
 */
public class AuthorizeResult {
    private boolean success;
    private String message;

    public AuthorizeResult(boolean isSuccess, String msg) {
        this.success = isSuccess;
        this.message = msg;
    }

    public boolean success() {
        return success;
    }

    public String message() {
        return message;
    }

}
