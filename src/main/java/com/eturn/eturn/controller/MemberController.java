package com.eturn.eturn.controller;

import com.eturn.eturn.service.PositionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value ="/member", produces = "application/json; charset=utf-8")
@Tag(name = "Участники", description = "Обработка участников конкретной очереди")
public class MemberController {

    private final PositionService positionService;

    public MemberController(PositionService positionService) {
        this.positionService = positionService;
    }

    @DeleteMapping("/{id}")
    public void deleteMember(HttpServletRequest request, @PathVariable String id){
        var authentication = (Authentication) request.getUserPrincipal();
        var userDetails = (UserDetails) authentication.getPrincipal();
        positionService.deleteMember(Long.parseLong(id), userDetails.getUsername());
    }
    @PutMapping()
    public void changeMemberAccess(HttpServletRequest request, @RequestParam Long id, @RequestParam String type){
        var authentication = (Authentication) request.getUserPrincipal();
        var userDetails = (UserDetails) authentication.getPrincipal();
        positionService.changeMemberStatus(id, type, userDetails.getUsername());
    }

    @PutMapping("/invite")
    public void inviteUser(HttpServletRequest request, @RequestParam String hash) {
        var authentication = (Authentication) request.getUserPrincipal();
        var userDetails = (UserDetails) authentication.getPrincipal();
        positionService.inviteUser(hash, userDetails.getUsername());
    }
    @PutMapping("/accept")
    public void acceptInvite(@RequestParam Long id, @RequestParam boolean status) {
        positionService.changeMemberInvite(id, status);
    }
}
