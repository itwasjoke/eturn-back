package com.eturn.eturn.exception.member;

public class UnknownMemberException extends RuntimeException{
    public UnknownMemberException(String message){
        super(message);
    }
}
