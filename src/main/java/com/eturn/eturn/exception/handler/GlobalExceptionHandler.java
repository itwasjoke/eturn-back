package com.eturn.eturn.exception.handler;

import com.eturn.eturn.exception.course.AlreadyExistCourseException;
import com.eturn.eturn.exception.course.NotFoundCourseException;
import com.eturn.eturn.exception.faculty.AlreadyExistFacultyException;
import com.eturn.eturn.exception.faculty.NotFoundFacultyException;
import com.eturn.eturn.exception.group.AlreadyExistGroupException;
import com.eturn.eturn.exception.group.NotFoundGroupException;
import com.eturn.eturn.exception.member.NoAccessMemberException;
import com.eturn.eturn.exception.member.NotFoundMemberException;
import com.eturn.eturn.exception.member.UnknownMemberException;
import com.eturn.eturn.exception.position.*;
import com.eturn.eturn.exception.turn.*;
import com.eturn.eturn.exception.user.AccessException;
import com.eturn.eturn.exception.user.AuthPasswordException;
import com.eturn.eturn.exception.user.LocalNotFoundUserException;
import com.eturn.eturn.exception.user.NotFoundUserException;
import jakarta.validation.ValidationException;
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

    private ResponseEntity<Object> buildResponse(
            Exception e,
            String body,
            HttpStatus status,
            WebRequest request
    ) {
        log.error("Error with this message: {}", e.getMessage());
        return handleExceptionInternal(
                e,
                body,
                new HttpHeaders(),
                status,
                request
        );
    }

    // Универсальный обработчик для исключений с разными сообщениями и статусами
    private ResponseEntity<Object> handleException(
            Exception e,
            WebRequest request,
            String body,
            HttpStatus status
    ) {
        return buildResponse(e, body, status, request);
    }

    // Примеры использования
    @ExceptionHandler({
            LocalNotFoundTurnException.class,
            LocalNotFoundUserException.class
    })
    public ResponseEntity<Object> handleBadRequestExceptions(
            Exception e,
            WebRequest request
    ) {
        return handleException(
                e,
                request,
                "Ошибка 400. Проверьте данные или авторизуйтесь заново.",
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler({
            NotFoundTurnException.class,
            NotFoundAllTurnsException.class,
            NotFoundUserException.class,
            NotFoundPosException.class,
            NotFoundMemberException.class,
            NotFoundGroupException.class,
            NotFoundFacultyException.class,
            NotFoundCourseException.class
    })
    public ResponseEntity<Object> handleNotFoundExceptions(
            Exception e,
            WebRequest request
    ) {
        return handleException(
                e,
                request,
                "Ресурс не найден.",
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler({
            NoAccessUpdateTurnException.class,
            NoAccessDeleteTurnException.class,
            NoAccessPosException.class,
            NoAccessMemberException.class
    })
    public ResponseEntity<Object> handleAccessDeniedExceptions(
            Exception e, WebRequest request
    ) {
        return handleException(
                e,
                request,
                "Нет прав для выполнения этого действия.",
                HttpStatus.METHOD_NOT_ALLOWED
        );
    }

    @ExceptionHandler({
            InvalidTypeTurnException.class,
            InvalidDataTurnException.class,
            InvalidTimeToCreateTurnException.class,
            InvalidLengthTurnException.class,
            NoSkipPositionException.class,
            NoInviteException.class,
            NoCreateTurnException.class
    })
    public ResponseEntity<Object> handleBadRequestValidationExceptions(
            Exception e,
            WebRequest request
    ) {
        return handleException(
                e,
                request,
                "Некорректные данные запроса.",
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler({ AuthPasswordException.class })
    public ResponseEntity<Object> handleAuthPasswordException(
            AuthPasswordException e,
            WebRequest request
    ) {
        return handleException(
                e,
                request,
                "Пароль введен неверно.",
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler({ AccessException.class })
    public ResponseEntity<Object> handleAccessException(
            AccessException e,
            WebRequest request
    ) {
        return handleException(
                e,
                request,
                "Нет доступа к этому ресурсу.",
                HttpStatus.FORBIDDEN
        );
    }

    @ExceptionHandler({
            AlreadyExistGroupException.class,
            AlreadyExistFacultyException.class,
            AlreadyExistCourseException.class
    })
    public ResponseEntity<Object> handleAlreadyExistExceptions(
            Exception e,
            WebRequest request
    ) {
        return handleException(
                e,
                request,
                "Такой ресурс уже существует.",
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler({ UnknownMemberException.class })
    public ResponseEntity<Object> handleUnknownMemberException(
            UnknownMemberException e,
            WebRequest request
    ) {
        return handleException(
                e,
                request,
                "Непредвиденная ошибка.",
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    @ExceptionHandler({
            ValidationException.class,
            NoCreatePosException.class
    })
    public ResponseEntity<Object> handleValidationException(
            ValidationException e,
            WebRequest request
    ) {
        return buildResponse(
                e,
                e.getMessage(),
                HttpStatus.BAD_REQUEST,
                request
        );
    }

}
