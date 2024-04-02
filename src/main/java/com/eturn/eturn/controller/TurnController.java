package com.eturn.eturn.controller;

import com.eturn.eturn.dto.MemberDTO;
import com.eturn.eturn.dto.TurnDTO;
import com.eturn.eturn.dto.TurnMoreInfoDTO;
import com.eturn.eturn.service.PositionService;
import com.eturn.eturn.service.TurnService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value ="/turn", produces = "application/json; charset=utf-8")
@Tag(name = "Очереди", description = "Обработка информации об очередях")
public class TurnController {

    private final TurnService turnService;
    private final PositionService positionService;

    public TurnController(TurnService turnService, PositionService positionService) {
        this.turnService = turnService;
        this.positionService = positionService;
    }

    @GetMapping("{idTurn}")
    @Operation(
            summary = "Получение очереди",
            description = "Основная информация об очереди без указания позиций"
    )
    public TurnDTO getTurn(@PathVariable long idTurn) {
        return turnService.getTurn(idTurn);
    }

    @GetMapping
    @Operation(
            summary = "Получение списка очередей",
            description = "На основе данных авторизации определяет текущего пользователя и параметров фильтрует список очередей под пользователя"
    )
    public List<TurnDTO> getUserTurns(
        HttpServletRequest request,
        @RequestParam @Parameter(description = "Тип очереди turn/edu") String type,
        @RequestParam @Parameter(description = "Тип доступа memberIn/memberOut") String access,
        @RequestParam(required = false) @Parameter(description = "Номер группы для фильтрации (необязательно)") String numberGroup,
        @RequestParam(required = false) @Parameter(description = "Идентификатор курса для фильтрации (необязательно)") String courseId,
        @RequestParam(required = false) @Parameter(description = "Идентификатор факультета для фильтрации (необязательно)") String facultyId
    ) {

        var authentication = (Authentication) request.getUserPrincipal();
        var userDetails = (UserDetails) authentication.getPrincipal();
        Map<String, String> params = new HashMap<>();
        params.put("Type", type);
        params.put("Access", access);

        if (numberGroup != null) {
            params.put("Group", numberGroup);
        }
        if (courseId != null) {
            params.put("Course", courseId);
        }
        if (facultyId != null) {
            params.put("Faculty", facultyId);
        }
        return turnService.getUserTurns(userDetails.getUsername(), params);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Создание очереди",
            description = "Получает объект и создает очередь"
    )
    public Long create(HttpServletRequest request, @RequestBody TurnMoreInfoDTO turn) {
        var authentication = (Authentication) request.getUserPrincipal();
        var userDetails = (UserDetails) authentication.getPrincipal();
        return turnService.createTurn(turn, userDetails.getUsername());
    }

    @PutMapping(value = "/member")
    @Operation(
            summary = "Добавление участника",
            description = "Добавляет пользователя к объекту очереди"
    )
    public void updateMember(
            HttpServletRequest request,
            @RequestParam @Parameter(description = "Идентификатор очереди") Long turnId
    ){
        var authentication = (Authentication) request.getUserPrincipal();
        var userDetails = (UserDetails) authentication.getPrincipal();
        turnService.addTurnToUser(turnId, userDetails.getUsername(), "MEMBER");
    }
    @GetMapping("/member")
    public MemberDTO getCurrentMember(
            HttpServletRequest request,
            @RequestParam Long turnId
    ){
        var authentication = (Authentication) request.getUserPrincipal();
        var userDetails = (UserDetails) authentication.getPrincipal();
        return turnService.getMember(userDetails.getUsername(), turnId);
    }
    @GetMapping("/members")
    public List<MemberDTO> getMemberList(
            HttpServletRequest request,
            @RequestParam String type, @RequestParam Long turnId
    ){
        var authentication = (Authentication) request.getUserPrincipal();
        var userDetails = (UserDetails) authentication.getPrincipal();
        return turnService.getMemberList(userDetails.getUsername(), type, turnId);
    }
    @DeleteMapping("/member/{id}")
    public void deleteMember(HttpServletRequest request, @PathVariable String id){
        var authentication = (Authentication) request.getUserPrincipal();
        var userDetails = (UserDetails) authentication.getPrincipal();
        positionService.deleteMember(Long.parseLong(id), userDetails.getUsername());
    }
    @PutMapping("/member/access")
    public void changeMemberAccess(HttpServletRequest request, @RequestParam Long id, @RequestParam String type){
        var authentication = (Authentication) request.getUserPrincipal();
        var userDetails = (UserDetails) authentication.getPrincipal();
        positionService.changeMemberStatus(id, type, userDetails.getUsername());
    }
//
//    @PutMapping()
//    public void update(
//        @RequestBody Turn turn,
//        @RequestParam long idUser
//    ) {
//        turnService.updateTurn(idUser, turn);
//    }

//    @DeleteMapping()
//    public void delete(
//        @RequestParam long idUser,
//        @RequestParam long idTurn
//    ) {
//        turnService.deleteTurn(idUser,idTurn);
//    }
}
