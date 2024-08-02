package com.eturn.eturn.controller;

import com.eturn.eturn.dto.*;
import com.eturn.eturn.service.PositionService;
import com.eturn.eturn.service.TurnService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
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

@Validated
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

    @GetMapping("/{hash}")
    @Operation(
            summary = "Получение текущей очереди",
            description = "Получает подробную информацию об очереди"
    )
    public TurnDTO getTurn(HttpServletRequest request, @PathVariable @Parameter(description = "Идентификатор очереди") String hash){
        var authentication = (Authentication) request.getUserPrincipal();
        var userDetails = (UserDetails) authentication.getPrincipal();
        return positionService.getTurn(hash, userDetails.getUsername());
    }

    @GetMapping
    @Operation(
            summary = "Получение списка очередей",
            description = "На основе данных авторизации определяет текущего пользователя и параметров фильтрует список очередей под пользователя"
    )
    public List<TurnForListDTO> getUserTurns(
        HttpServletRequest request,
        @RequestParam @Parameter(name = "type22", description = "Тип очереди turn/edu") String type,
        @RequestParam @Parameter(description = "Тип доступа memberIn/memberOut") String access
    ) {

        var authentication = (Authentication) request.getUserPrincipal();
        var userDetails = (UserDetails) authentication.getPrincipal();
        Map<String, String> params = new HashMap<>();
        params.put("Type", type);
        params.put("Access", access);
        return turnService.getUserTurns(userDetails.getUsername(), params);
    }
    @GetMapping("/linked")
    public List<TurnForListDTO> getLinkedTurn(HttpServletRequest request, @RequestParam String hash){
        var authentication = (Authentication) request.getUserPrincipal();
        var userDetails = (UserDetails) authentication.getPrincipal();
        return turnService.getLinkedTurn(hash, userDetails.getUsername());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Создание очереди",
            description = "Получает объект и создает очередь"
    )
    public String create(HttpServletRequest request,@Valid @RequestBody TurnCreatingDTO turn, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            List<FieldError> errors = bindingResult.getFieldErrors();
            throw new ValidationException("Ошибка валидации:", (Throwable) errors);
        }
        var authentication = (Authentication) request.getUserPrincipal();
        var userDetails = (UserDetails) authentication.getPrincipal();
        return turnService.createTurn(turn, userDetails.getUsername());
    }
    @GetMapping("/members")
    public List<MemberDTO> getMemberList(
            HttpServletRequest request,
            @RequestParam String type, @RequestParam String hash, @RequestParam int page
    ){
        var authentication = (Authentication) request.getUserPrincipal();
        var userDetails = (UserDetails) authentication.getPrincipal();
        return turnService.getMemberList(userDetails.getUsername(), type, hash, page);
    }

    @GetMapping("/unconfMembers")
    public List<MemberDTO> getUnconfMembers(
            HttpServletRequest request,
            @RequestParam String type, @RequestParam String hash
    ) {
        var authentication = (Authentication) request.getUserPrincipal();
        var userDetails = (UserDetails) authentication.getPrincipal();
        return turnService.getUnconfMemberList(userDetails.getUsername(), type, hash);
    }

    @DeleteMapping("/{hash}")
    public void delete(
            HttpServletRequest request,
            @PathVariable String hash
    ) {
        var authentication = (Authentication) request.getUserPrincipal();
        var userDetails = (UserDetails) authentication.getPrincipal();
        turnService.deleteTurn(userDetails.getUsername(), hash);
    }

    @PutMapping()
    public void changeTurn(HttpServletRequest request, @Valid @RequestBody TurnEditDTO turn, BindingResult bindingResult){
        if (bindingResult.hasErrors()) {
            List<FieldError> errors = bindingResult.getFieldErrors();
            throw new ValidationException("Ошибка валидации:", (Throwable) errors);
        }
        var authentication = (Authentication) request.getUserPrincipal();
        var userDetails = (UserDetails) authentication.getPrincipal();
        turnService.changeTurn(turn, userDetails.getUsername());
    }

}
