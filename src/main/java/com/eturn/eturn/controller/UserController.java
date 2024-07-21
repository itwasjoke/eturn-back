package com.eturn.eturn.controller;


import com.eturn.eturn.dto.TurnDTO;
import com.eturn.eturn.dto.UserCreateDTO;
import com.eturn.eturn.dto.UserDTO;
import com.eturn.eturn.entity.User;
import com.eturn.eturn.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/user", produces = "application/json; charset=utf-8")
@Tag(name = "Пользователь", description = "Работа с информацией об участнике")
public class UserController {

    private final UserService userService;



    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping()
    @Operation(
            summary = "Получение пользователя",
            description = "На основе данных авторизации определяет текущего пользователя и возврашает объект"
    )
    public UserDTO getUser(HttpServletRequest request){
        var authentication = (Authentication) request.getUserPrincipal();
        var userDetails = (UserDetails) authentication.getPrincipal();
        return userService.getUser(userDetails.getUsername());
    }
}
