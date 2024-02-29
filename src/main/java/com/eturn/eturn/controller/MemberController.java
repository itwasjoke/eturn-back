package com.eturn.eturn.controller;

import com.eturn.eturn.entity.Member;
import com.eturn.eturn.enums.AccessMemberEnum;
import com.eturn.eturn.service.MemberService;
import com.eturn.eturn.service.TurnService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/member")
@Tag(name = "Участники", description = "Администрирование пользователей внутри очереди")
public class MemberController {
    private final MemberService memberService;
    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }



//    @PostMapping
//    public void create(@RequestParam Long userId, @RequestParam Long turnId, @RequestParam String accessMemberEnum){
//        AccessMemberEnum access = AccessMemberEnum.valueOf(accessMemberEnum);
//        memberService.createMember(userId, turnId, access);
//    }
}
