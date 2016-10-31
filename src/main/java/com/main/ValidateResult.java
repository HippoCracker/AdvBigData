package com.main;

public class ValidateResult {

  private boolean success;
  private String message;

  public ValidateResult(boolean isSuccess, String msg) {
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
