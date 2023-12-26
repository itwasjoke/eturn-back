package com.eturn.eturn.controller;


import com.eturn.eturn.dto.TurnDTO;
import com.eturn.eturn.dto.UserCreateDTO;
import com.eturn.eturn.dto.UserDTO;
import com.eturn.eturn.entity.User;
import com.eturn.eturn.exception.InvalidDataException;
import com.eturn.eturn.security.JwtTokenRepository;
import com.eturn.eturn.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/user", produces = "application/json; charset=utf-8")
public class UserController {

    private final UserService userService;

    private final JwtTokenRepository jwtTokenRepository;


    public UserController(UserService userService, JwtTokenRepository jwtTokenRepository) {
        this.userService = userService;
        this.jwtTokenRepository = jwtTokenRepository;
    }

    @GetMapping(value = "/{id}", produces = "application/json; charset=utf-8")
    public UserDTO getUser(@PathVariable long id){
        return userService.getUser(id);
    }

//    @GetMapping("/{id}")
//    public User getUser(@PathVariable("id") User user){return user;}

    @PostMapping
    public Long create(@RequestBody UserCreateDTO user){
        return userService.createUser(user);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") User user){

    }

    @GetMapping(value = "/turns/{id}")
    public List<TurnDTO> getUsersTurns(@PathVariable long id){
        return userService.getUserTurnsDTO(id);
    }

    @PostMapping("/login")
    public CsrfToken login(HttpServletRequest httpServletRequest){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return null;
        }
        Object principal = auth.getPrincipal();
        org.springframework.security.core.userdetails.User user = (principal instanceof org.springframework.security.core.userdetails.User) ? (org.springframework.security.core.userdetails.User) principal : null;
        if (user!=null){
            return jwtTokenRepository.loadToken(httpServletRequest);
        }
        else{
            throw new InvalidDataException("No auth");
        }

    }

    @PostMapping("/register")
    public Long register(@RequestBody UserCreateDTO user){
        return userService.createUser(user);
    }
}
