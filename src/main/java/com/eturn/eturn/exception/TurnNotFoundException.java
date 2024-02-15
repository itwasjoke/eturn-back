package com.eturn.eturn.exception;

public class TurnNotFoundException extends RuntimeException {
    public TurnNotFoundException(String message){
        super(message);
    }
}
