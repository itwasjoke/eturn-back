package com.eturn.eturn.exception.handler;

import com.eturn.eturn.exception.InvalidTypeTurnException;
import com.eturn.eturn.exception.TurnNotFoundException;
import com.eturn.eturn.exception.UnknownException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(UnknownException.class)
    public ResponseEntity<Object> handleUnknownException(UnknownException e, WebRequest request) {
        log.error("Error with this message: " + e.getMessage());
        String body = "Оу... неприятно, что-то пошло не так.";
        return handleExceptionInternal(e, body, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }
    @ExceptionHandler(TurnNotFoundException.class)
    public ResponseEntity<Object> handleTurnNotFoundException(TurnNotFoundException e, WebRequest request) {
        log.error("Error with this message: " + e.getMessage());
        String body = "Очередь не найдена.";
        return handleExceptionInternal(e, body, new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }
    @ExceptionHandler(InvalidTypeTurnException.class)
    public ResponseEntity<Object> handleInvalidTypeTurnException(InvalidTypeTurnException e, WebRequest request) {
        log.error("Error with this message: " + e.getMessage());
        String body = "Тип очереди указан неправильно. Разработчики начудили...";
        return handleExceptionInternal(e, body, new HttpHeaders(), HttpStatus.NOT_ACCEPTABLE, request);
    }
}
