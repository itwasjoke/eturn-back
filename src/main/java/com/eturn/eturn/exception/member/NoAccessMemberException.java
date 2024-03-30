package com.eturn.eturn.exception.member;

public class NoAccessMemberException extends RuntimeException {
    public NoAccessMemberException(String message){
        super(message);
    }
}
