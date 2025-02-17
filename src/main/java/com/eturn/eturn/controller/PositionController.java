package com.eturn.eturn.controller;

import com.eturn.eturn.dto.DetailedPositionDTO;
import com.eturn.eturn.dto.PositionsTurnDTO;
import com.eturn.eturn.service.PositionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value ="/position", produces = "application/json; charset=utf-8")
@Tag(name = "Позиции", description = "Обработка позиций у конкретной очереди")
public class PositionController {
    private final PositionService positionService;

    public PositionController(PositionService positionService) {
        this.positionService = positionService;
    }

    @GetMapping ()
    @Operation(
            summary = "Получение позиций очереди",
            description = "Отправляет список позиций, которые принадлежат определенной очереди"
    )
    public PositionsTurnDTO getTurnPositions(
            HttpServletRequest request,
            @RequestParam @Parameter(name = "hash", description = "Хэш очереди") String hash,
            @RequestParam(defaultValue = "0") @Parameter(name = "page", description = "Номер страницы (пагинация)") int page
    ){
        var authentication = (Authentication) request.getUserPrincipal();
        var userDetails = (UserDetails) authentication.getPrincipal();
        return positionService.getPositionList(hash, userDetails.getUsername(), page);
    }
    @PostMapping("/{hash}")
    @Operation(
            summary = "Создание позиции",
            description = "Берет текущего авторизированного пользователя и создает позицию для него"
    )
    public DetailedPositionDTO createPosition(
            HttpServletRequest request,
            @PathVariable @Parameter(name = "hash", description = "Хэш очереди") String hash
    ){
        var authentication = (Authentication) request.getUserPrincipal();
        var userDetails = (UserDetails) authentication.getPrincipal();
        return positionService.createPositionAndSave(userDetails.getUsername(), hash);
    }

    @PutMapping()
    @Operation(
            summary = "Изменение статуса позиции",
            description = "Находит позицию и изменяет ее статус из 'вход' на 'выход' и с 'выход' на удаление позиции"
    )
    public void updateStatus(
            HttpServletRequest request,
            @RequestParam @Parameter(name = "id", description = "ID позиции") Long id,
            @RequestParam @Parameter(name = "status", description = "Статус (in/out)") String status
    ){
        var authentication = (Authentication) request.getUserPrincipal();
        var userDetails = (UserDetails) authentication.getPrincipal();
        positionService.update(id, userDetails.getUsername(), status);
    }

    @PutMapping("/skip/{id}")
    @Operation(
            summary = "Пропустить позицию",
            description = "Пропускает следующего человека вперёд"
    )
    public void skipPosition(
            HttpServletRequest request,
            @PathVariable @Parameter(name = "id", description = "Идентификатор позиции") Long id
    ){
        var authentication = (Authentication) request.getUserPrincipal();
        var userDetails = (UserDetails) authentication.getPrincipal();
        positionService.skipPosition(id, userDetails.getUsername());
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Удаление позиции",
            description = "Удаляет позицию по id"
    )
    public void deletePosition(
            HttpServletRequest request,
            @PathVariable @Parameter(name = "id", description = "Идентификатор позиции") Long id){
        var authentication = (Authentication) request.getUserPrincipal();
        var userDetails = (UserDetails) authentication.getPrincipal();
        positionService.delete(id, userDetails.getUsername());
    }
}
