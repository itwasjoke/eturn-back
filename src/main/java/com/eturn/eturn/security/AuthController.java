package com.eturn.eturn.security;

import com.eturn.eturn.dto.UserCreateDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Авторизация", description = "Вход и регистрация")
public class AuthController {

    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @GetMapping("/test")
    @Operation(
            summary = "Проверка вывода данных о пользователе",
            description = "Вывод имени авторизированного пользователя"
    )
    String getGreetings(@RequestParam String text, HttpServletRequest request) {
        // Получаем результат аутентификации
        var authentication = (Authentication) request.getUserPrincipal();
        // Получаем информацию о пользователе
        var userDetails = (UserDetails) authentication.getPrincipal();
        // Используем
        return "Hello "+userDetails.getUsername()+" "+text;
    }

    @PostMapping("/sign-up")
    @Operation(
            summary = "Регистрация",
            description = "Создание объекта пользователя и возвращение токена авторизации"
    )
    public JwtAuthenticationResponse signUp(@RequestBody UserCreateDTO request){
        return authenticationService.signUp(request);
    }

    @PostMapping("/sign-in")
    @Operation(
            summary = "Вход",
            description = "Отправка логина и пароля, получение токена авторизации"
    )
    public JwtAuthenticationResponse signIn(
            @RequestParam @Parameter(description = "Логин") String login,
            @RequestParam @Parameter(description = "Пароль") String password
    ){
        return authenticationService.signIn(login, password);
    }

}
