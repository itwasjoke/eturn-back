package com.eturn.eturn.controller;

import com.eturn.eturn.service.PositionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
    @Operation(
            summary = "Удаление участника",
            description = "Удаляет участника по id"
    )
    public void deleteMember(
            HttpServletRequest request,
            @PathVariable @Parameter(name = "id", description = "Идентификатор участника") String id
    ){
        var authentication = (Authentication) request.getUserPrincipal();
        var userDetails = (UserDetails) authentication.getPrincipal();
        positionService.deleteMember(Long.parseLong(id), userDetails.getUsername());
    }
    @PutMapping()
    @Operation(
            summary = "Изменение типа доступа участника",
            description = "Изменяет тип доступа участника (MEMBER/BLOCKED) по id"
    )
    public void changeMemberAccess(
            HttpServletRequest request,
            @RequestParam @Parameter(name = "id", description = "Идентификатор участника") Long id,
            @RequestParam @Parameter(name = "type", description = "Тип доступа (MEMBER/BLOCKED)") String type
    ){
        var authentication = (Authentication) request.getUserPrincipal();
        var userDetails = (UserDetails) authentication.getPrincipal();
        positionService.changeMemberStatus(id, type, userDetails.getUsername());
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
        positionService.inviteUser(hash, userDetails.getUsername());
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
        positionService.changeMemberInvite(id, status, isModerator);
    }
}
