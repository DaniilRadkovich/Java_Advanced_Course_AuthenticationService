package com.innowise.authenticationservice.exception;

public class WrongPasswordException extends RuntimeException{
  public WrongPasswordException(String message) {
    super(message);
  }
}
