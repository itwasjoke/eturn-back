package com.eturn.eturn.exception.user;

public class NotFoundUserException extends RuntimeException{
    public NotFoundUserException(String message){
        super(message);
    }
}
