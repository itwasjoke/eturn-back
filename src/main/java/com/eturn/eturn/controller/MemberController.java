package com.eturn.eturn.controller;

import com.eturn.eturn.dto.MemberListDTO;
import com.eturn.eturn.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/member", produces = "application/json; charset=utf-8")
@Tag(name = "Участники", description = "Обработка участников конкретной очереди")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PutMapping()
    @Operation(
            summary = "Блокировка или разблокировка пользователя",
            description = "Изменяет тип доступа участника (MEMBER/BLOCKED) по id"
    )
    public void setBlockStatus(
            HttpServletRequest request,
            @RequestParam @Parameter(name = "id", description = "Идентификатор участника") Long id,
            @RequestParam @Parameter(name = "type", description = "Тип доступа (MEMBER/BLOCKED)") String type
    ){
        var authentication = (Authentication) request.getUserPrincipal();
        var userDetails = (UserDetails) authentication.getPrincipal();
        memberService.setBlockStatus(id, type, userDetails.getUsername());
    }

    @PutMapping("/invite")
    @Operation(
            summary = "Заявка на становление модератором",
            description = "Подача заяки на становление модератором (меняет тип доступа на MEMBER_LINK и invited на true)"
    )
    public void inviteUser(
            HttpServletRequest request,
            @RequestParam @Parameter(name = "hash", description = "Хэш очереди") String hash
    ) {
        var authentication = (Authentication) request.getUserPrincipal();
        var userDetails = (UserDetails) authentication.getPrincipal();
        memberService.setInviteForMember(hash, userDetails.getUsername());
    }
    @PutMapping("/accept")
    @Operation(
            summary = "Принятие заявки (на модератора или в очередь по ссылке)",
            description = "Принимает или отклоняет зявку на становление модератором или в очередь по ссылке (меняет тип доступа и invited/invitedForTurn на false)"
    )
    public void acceptInvite(
            @RequestParam @Parameter(name = "id", description = "Идентификатор участника") Long id,
            @RequestParam @Parameter(name = "status", description = "Решение по заявке (принять/отклонить)") boolean status,
            @RequestParam @Parameter(name = "isModerator", description = "Подтверждение модератора (true) или участника (false)") boolean isModerator
    ) {
        memberService.changeMemberInvite(id, status, isModerator);
    }

    @GetMapping("/list")
    @Operation(
            summary = "Получение списка участников",
            description = "Выводит список участников (участник/модератор/заблокированный/по ссылке) очереди"
    )
    public MemberListDTO getMemberList(
            HttpServletRequest request,
            @RequestParam @Parameter(name = "type", description = "Тип участника MEMBER/MODERATOR/BLOCKED") String type,
            @RequestParam @Parameter(name = "hash", description = "Хэш очереди") String hash,
            @RequestParam @Parameter(name = "page", description = "Номер страницы (пагинация)") int page
    ){
        var authentication = (Authentication) request.getUserPrincipal();
        var userDetails = (UserDetails) authentication.getPrincipal();
        return memberService.getMemberList(userDetails.getUsername(), type, hash, page);
    }

    @GetMapping("/unconfirmed")
    @Operation(
            summary = "Получение списка неподтверждённых участников",
            description = "Выводит список тех, кто подал заявку (на модератора или в очередь по ссылке), но его пока не подтвердили"
    )
    public MemberListDTO getUnconfirmedMembers(
            HttpServletRequest request,
            @RequestParam @Parameter(name = "type", description = "Тип участника MEMBER/MODERATOR") String type,
            @RequestParam @Parameter(name = "hash", description = "Хэш очереди") String hash
    ) {
        var authentication = (Authentication) request.getUserPrincipal();
        var userDetails = (UserDetails) authentication.getPrincipal();
        return memberService.getUnconfirmedMemberList(userDetails.getUsername(), type, hash);
    }
}
