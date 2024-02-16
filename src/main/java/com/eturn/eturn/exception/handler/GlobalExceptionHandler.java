package com.eturn.eturn.exception.handler;

import com.eturn.eturn.exception.course.AlreadyExistCourseException;
import com.eturn.eturn.exception.course.NotFoundCourseException;
import com.eturn.eturn.exception.faculty.AlreadyExistFacultyException;
import com.eturn.eturn.exception.faculty.NotFoundFacultyException;
import com.eturn.eturn.exception.group.AlreadyExistGroupException;
import com.eturn.eturn.exception.group.NotFoundGroupException;
import com.eturn.eturn.exception.member.NotFoundMemberException;
import com.eturn.eturn.exception.member.UnknownMemberException;
import com.eturn.eturn.exception.position.NoCreatePosException;
import com.eturn.eturn.exception.position.NotFoundPosException;
import com.eturn.eturn.exception.turn.*;
import com.eturn.eturn.exception.user.AuthPasswordException;
import com.eturn.eturn.exception.user.LocalNotFoundUserException;
import com.eturn.eturn.exception.user.NotFoundUserException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    //
    // TURNS
    //
    @ExceptionHandler(LocalNotFoundTurnException.class)
    public ResponseEntity<Object> handleUnknownException(LocalNotFoundTurnException e, WebRequest request) {
        log.error("Error with this message: " + e.getMessage());
        String body = "Ошибка. Не можем выполнить запрос, потому что Вы хотите использовать очередь, которой не существует. Обновите страницу или сообщите в техническую поддержку.";
        return handleExceptionInternal(e, body, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }
    @ExceptionHandler(NotFoundTurnException.class)
    public ResponseEntity<Object> handleTurnNotFoundException(NotFoundTurnException e, WebRequest request) {
        log.error("Error with this message: " + e.getMessage());
        String body = "Очередь не найдена.";
        return handleExceptionInternal(e, body, new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }
    @ExceptionHandler(NotFoundAllTurnsException.class)
    public ResponseEntity<Object> handleTurnNotFoundException(NotFoundAllTurnsException e, WebRequest request) {
        log.error("Error with this message: " + e.getMessage());
        String body = "Очереди не найдены.";
        return handleExceptionInternal(e, body, new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }
    @ExceptionHandler(InvalidTypeTurnException.class)
    public ResponseEntity<Object> handleInvalidTypeTurnException(InvalidTypeTurnException e, WebRequest request) {
        log.error("Error with this message: " + e.getMessage());
        String body = "Ошибка 400. Сообщите в техническую поддержку о проблеме.";
        return handleExceptionInternal(e, body, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }
    @ExceptionHandler(NoAccessUpdateTurnException.class)
    public ResponseEntity<Object> handleNoAccessUpdateTurnException(NoAccessUpdateTurnException e, WebRequest request) {
        log.error("Error with this message: " + e.getMessage());
        String body = "Нет прав для обновления информации об очереди.";
        return handleExceptionInternal(e, body, new HttpHeaders(), HttpStatus.METHOD_NOT_ALLOWED, request);
    }
    @ExceptionHandler(NoAccessDeleteTurnException.class)
    public ResponseEntity<Object> handleNoAccessDeleteTurnException(NoAccessDeleteTurnException e, WebRequest request) {
        log.error("Error with this message: " + e.getMessage());
        String body = "Нет прав для удаления очереди. Удалить очередь может лишь создатель.";
        return handleExceptionInternal(e, body, new HttpHeaders(), HttpStatus.METHOD_NOT_ALLOWED, request);
    }

    //
    // USERS
    //
    @ExceptionHandler(NotFoundUserException.class)
    public ResponseEntity<Object> handleNotFoundUserException(NotFoundUserException e, WebRequest request) {
        log.error("Error with this message: " + e.getMessage());
        String body = "Пользователь не найден.";
        return handleExceptionInternal(e, body, new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }
    @ExceptionHandler(LocalNotFoundUserException.class)
    public ResponseEntity<Object> handleLocalNotFoundUserException(LocalNotFoundUserException e, WebRequest request) {
        log.error("Error with this message: " + e.getMessage());
        String body = "Ошибка 400. Не можем выполнить запрос, потому что Вы хотите использовать пользователя, которого не существует. Пройдите авторизацию заново.";
        return handleExceptionInternal(e, body, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(AuthPasswordException.class)
    public ResponseEntity<Object> handleAuthPasswordException(AuthPasswordException e, WebRequest request) {
        log.error("Error with this message: " + e.getMessage());
        String body = "Пароль введен неверно.";
        return handleExceptionInternal(e, body, new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }

    //
    // POSITIONS
    //

    @ExceptionHandler(NoCreatePosException.class)
    public ResponseEntity<Object> handleNoCreatePosException(NoCreatePosException e, WebRequest request) {
        String body = "Вы уже вставали в очередь недавно. Вы сможете занять место через " + e.getMessage() +" позиций.";
        return handleExceptionInternal(e, body, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(NotFoundPosException.class)
    public ResponseEntity<Object> handleNotFoundPosException(NotFoundPosException e, WebRequest request) {
        String body = "Позиции не найдены.";
        return handleExceptionInternal(e, body, new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }

    //
    // MEMBER
    //

    @ExceptionHandler(NotFoundMemberException.class)
    public ResponseEntity<Object> handleNotFoundMemberException(NotFoundMemberException e, WebRequest request) {
        String body = "Участник очереди не найден.";
        log.error("Error with this message: " + e.getMessage());
        return handleExceptionInternal(e, body, new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(UnknownMemberException.class)
    public ResponseEntity<Object> handleUnknownMemberException(UnknownMemberException e, WebRequest request) {
        String body = "Непредвиденная ошибка.";
        log.error("Error with this message: " + e.getMessage());
        return handleExceptionInternal(e, body, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    //
    // GROUP
    //

    @ExceptionHandler(NotFoundGroupException.class)
    public ResponseEntity<Object> handleNotFoundGroupException(NotFoundGroupException e, WebRequest request) {
        String body = "Группа не найдена.";
        log.error("Error with this message: " + e.getMessage());
        return handleExceptionInternal(e, body, new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(AlreadyExistGroupException.class)
    public ResponseEntity<Object> handleAlreadyExistGroupException(AlreadyExistGroupException e, WebRequest request) {
        String body = "Такая группа уже существует.";
        log.error("Error with this message: " + e.getMessage());
        return handleExceptionInternal(e, body, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    //
    // FACULTY
    //

    @ExceptionHandler(NotFoundFacultyException.class)
    public ResponseEntity<Object> handleNotFoundFacultyException(NotFoundFacultyException e, WebRequest request) {
        String body = "Факультет не найден.";
        log.error("Error with this message: " + e.getMessage());
        return handleExceptionInternal(e, body, new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(AlreadyExistFacultyException.class)
    public ResponseEntity<Object> handleAlreadyExistFacultyException(AlreadyExistFacultyException e, WebRequest request) {
        String body = "Такой факультет уже существует.";
        log.error("Error with this message: " + e.getMessage());
        return handleExceptionInternal(e, body, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    //
    // COURSE
    //
    @ExceptionHandler(NotFoundCourseException.class)
    public ResponseEntity<Object> handleNotFoundCourseException(NotFoundCourseException e, WebRequest request) {
        String body = "Курс не найден.";
        log.error("Error with this message: " + e.getMessage());
        return handleExceptionInternal(e, body, new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(AlreadyExistCourseException.class)
    public ResponseEntity<Object> handleAlreadyExistCourseException(AlreadyExistCourseException e, WebRequest request) {
        String body = "Такой курс уже существует.";
        log.error("Error with this message: " + e.getMessage());
        return handleExceptionInternal(e, body, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

}
