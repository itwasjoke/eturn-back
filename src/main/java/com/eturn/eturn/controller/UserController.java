package com.eturn.eturn.controller;


import com.eturn.eturn.dto.UserCreateDTO;
import com.eturn.eturn.dto.UserDTO;
import com.eturn.eturn.entity.User;
import com.eturn.eturn.enums.RoleEnum;
import com.eturn.eturn.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/user", produces = "application/json; charset=utf-8")
public class UserController {

    private final UserService userService;

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



}
