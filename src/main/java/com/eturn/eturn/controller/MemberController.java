package com.eturn.eturn.controller;


import com.eturn.eturn.entity.Group;
import com.eturn.eturn.entity.Member;
import com.eturn.eturn.entity.User;
import com.eturn.eturn.repository.GroupRepository;
import com.eturn.eturn.repository.MemberRepository;
import com.eturn.eturn.repository.UserRepository;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@RestController
@RequestMapping("member")
public class MemberController {


    @GetMapping
    public List<User> getMemberList() {
        return null;
    }

    @GetMapping("root")
    public Member checkRootUser() {
        return null;
    }

    @PostMapping
    public Member create(@RequestBody Member member) {
        return null;
    }

    @PutMapping()
    public Member update() {
        return null;
    }


    @DeleteMapping()
    public void delete() {


    }


}
