package com.eturn.eturn.controller;


import com.eturn.eturn.dto.TurnDTO;
import com.eturn.eturn.dto.UserCreateDTO;
import com.eturn.eturn.dto.UserDTO;
import com.eturn.eturn.entity.Turn;
import com.eturn.eturn.entity.User;
import com.eturn.eturn.enums.RoleEnum;
import com.eturn.eturn.security.CustomUserDetailsService;
import com.eturn.eturn.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping(value = "/user", produces = "application/json; charset=utf-8")
public class UserController {

    private final UserService userService;
    @Autowired
    private UserDetailsService userDetailsService;


    public UserController(UserService userService) {
        this.userService = userService;
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

    @GetMapping("/login")
    public Long login(Model model, UserCreateDTO user){
        return null;
    }

}
