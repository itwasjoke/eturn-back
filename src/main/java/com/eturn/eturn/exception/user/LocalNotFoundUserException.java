package com.eturn.eturn.exception.user;

public class LocalNotFoundUserException extends RuntimeException{
    public LocalNotFoundUserException(String message){
        super(message);
    }
}
