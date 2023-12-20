package com.eturn.eturn.controller;


import com.eturn.eturn.dto.UserDTO;
import com.eturn.eturn.entity.User;
import com.eturn.eturn.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public UserDTO getUser(@PathVariable long id){
        return userService.getUser(id);
    }

//    @GetMapping("/{id}")
//    public User getUser(@PathVariable("id") User user){return user;}

    @PostMapping
    public Long create(@RequestBody User user){
        return userService.createUser(user);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") User user){

    }



}
