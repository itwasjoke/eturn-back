package com.eturn.eturn.controller;

import com.eturn.eturn.entity.Member;
import com.eturn.eturn.enums.AccessMemberEnum;
import com.eturn.eturn.service.MemberService;
import com.eturn.eturn.service.TurnService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
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

    @GetMapping
    public Member getCurrentMember(
            HttpServletRequest request,
            @RequestParam Long turnId
    ){
        var authentication = (Authentication) request.getUserPrincipal();
        var userDetails = (UserDetails) authentication.getPrincipal();
        return memberService.getMember(userDetails.getUsername(), turnId);
    }

//    @PostMapping
//    public void create(@RequestParam Long userId, @RequestParam Long turnId, @RequestParam String accessMemberEnum){
//        AccessMemberEnum access = AccessMemberEnum.valueOf(accessMemberEnum);
//        memberService.createMember(userId, turnId, access);
//    }
}
