package com.eturn.eturn.security;

import com.eturn.eturn.dto.AuthData;
import com.eturn.eturn.dto.UserCreateDTO;
import com.eturn.eturn.security.jwt.JwtAuthenticationResponse;
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

    @PostMapping("/sign-up")
    @Operation(
            summary = "Регистрация",
            description = "Создание объекта пользователя и возвращение токена авторизации"
    )
    public JwtAuthenticationResponse signUp(
            @RequestBody UserCreateDTO user,
            HttpServletRequest request){
        var authentication = (Authentication) request.getUserPrincipal();
        var userDetails = (UserDetails) authentication.getPrincipal();
        return authenticationService.signUp(user, userDetails.getUsername());
    }

    @PostMapping("/sign-in")
    @Operation(
            summary = "Вход",
            description = "Отправка логина и пароля, получение токена авторизации"
    )
    public JwtAuthenticationResponse signIn(
            @RequestParam @Parameter(name = "login", description = "Логин") String login,
            @RequestParam @Parameter(name = "password", description = "Пароль") String password
    ){
        return authenticationService.signIn(login, password);
    }

    @PostMapping("/groups")
    public void createGroups(HttpServletRequest request) {
        var authentication = (Authentication) request.getUserPrincipal();
        var userDetails = (UserDetails) authentication.getPrincipal();
        authenticationService.createFaculties(userDetails.getUsername());
    }

    @PostMapping("/etuid")
    @Operation(
            summary = "Вход",
            description = "Отправка логина и пароля, получение токена авторизации"
    )
    public JwtAuthenticationResponse signIn(@RequestBody AuthData authData){
        return authenticationService.auth(authData);
    }

}
