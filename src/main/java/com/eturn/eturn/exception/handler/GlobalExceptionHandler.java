package com.eturn.eturn.exception.handler;

import com.eturn.eturn.exception.AccessException;
import com.eturn.eturn.exception.InvalidDataException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({ AccessException.class, InvalidDataException.class })
    public void handleAccessDeniedException(AccessException e) {
        log.error("Some exception occurred while execute method: methodName with message: " + e.getMessage());
    }
}
