package com.eturn.eturn.security;

import com.eturn.eturn.dto.UserCreateDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @GetMapping("/test")
    String getGreetings(@RequestParam String text, HttpServletRequest request) {
        // Получаем результат аутентификации
        var authentication = (Authentication) request.getUserPrincipal();
        // Получаем информацию о пользователе
        var userDetails = (UserDetails) authentication.getPrincipal();
        // Используем
        return "Hello "+userDetails.getUsername()+" "+text;
    }

    @PostMapping("/sign-up")
    public JwtAuthenticationResponse signUp(@RequestBody UserCreateDTO request){
        return authenticationService.signUp(request);
    }

    @PostMapping("/sign-in")
    public JwtAuthenticationResponse signIn(@RequestParam String login, @RequestParam String password){
        return authenticationService.signIn(login, password);
    }

}
