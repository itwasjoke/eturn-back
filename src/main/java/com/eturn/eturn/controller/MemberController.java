package com.eturn.eturn.controller;

import com.eturn.eturn.enums.AccessMemberEnum;
import com.eturn.eturn.service.MemberService;
import com.eturn.eturn.service.TurnService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/member")
public class MemberController {
    private final MemberService memberService;
    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping
    public void create(@RequestParam Long userId, @RequestParam Long turnId, @RequestParam AccessMemberEnum accessMemberEnum){
        memberService.createMember(userId, turnId, accessMemberEnum);
    }
}
