package com.eturn.eturn.controller;


import com.eturn.eturn.entity.Turn;
import com.eturn.eturn.entity.User;
import com.eturn.eturn.repository.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("user")
public class UserController {

    @GetMapping
    public List<User> getUserList(){return null;}

    @PostMapping
    public User create(@RequestBody User user){
        return null;
    }

    @DeleteMapping("{id}")
    public void delete(@PathVariable("id") User user){

    }



}
