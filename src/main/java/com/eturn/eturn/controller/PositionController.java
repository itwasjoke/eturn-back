package com.eturn.eturn.controller;

import com.eturn.eturn.dto.PositionDTO;
import com.eturn.eturn.dto.PositionMoreInfoDTO;
import com.eturn.eturn.service.PositionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value ="/position", produces = "application/json; charset=utf-8")
@Tag(name = "Позиции", description = "Обработка позиций у конкретной очереди")
public class PositionController {
    private final PositionService positionService;

    public PositionController(PositionService positionService) {
        this.positionService = positionService;
    }
    // TODO Надо ли сохранять?

    //    @GetMapping("{id}")
    //    public PositionDTO getPosition(@PathVariable Long id){
    //        return positionService.getPositionById(id);
    //    }


    // TODO Сделать через авторизацию
    @GetMapping(value = "/first")
    @Operation(
            summary = "Получение первой позиции",
            description = "Отправляет объект с сущностью первой позиции пользователя"
    )
    public PositionMoreInfoDTO getUserFirstPosition(
            HttpServletRequest request,
            @RequestParam @Parameter(description = "Идентификатор очереди")Long turnId){
        var authentication = (Authentication) request.getUserPrincipal();
        var userDetails = (UserDetails) authentication.getPrincipal();

        return positionService.getFirstUserPosition(turnId, userDetails.getUsername());
    }

    @GetMapping ("/all/{idTurn}")
    @Operation(
            summary = "Получение позиций очереди",
            description = "Отправляет список позиций, которые принадлежат определенной очереди"
    )
    public List<PositionDTO> getTurnPositions(
            @PathVariable @Parameter(description = "Идентификатор очереди") Long idTurn,
            @RequestParam(defaultValue = "0") @Parameter(description = "Страница позиций") int page
    ){
        return positionService.getPositionList(idTurn, page);
    }
    @PostMapping
    @Operation(
            summary = "Создание позиции",
            description = "Берет текущего авторизированного пользователя и создает позицию для него"
    )
    public PositionMoreInfoDTO createPosition(
            HttpServletRequest request,
            @RequestParam @Parameter(description = "Идентификатор очереди") Long idTurn
    ){
        var authentication = (Authentication) request.getUserPrincipal();
        var userDetails = (UserDetails) authentication.getPrincipal();
        return positionService.createPositionAndSave(userDetails.getUsername(),idTurn);
    }

    @PutMapping()
    @Operation(
            summary = "Изменение статуса позиции",
            description = "Находит позицию и изменяет ее статус из 'вход' на 'выход' и с 'выход' на удаление позиции"
    )
    public void updateStatus(
            @RequestParam @Parameter(description = "Идентификатор позиции") Long id
    ){
        positionService.update(id);
    }
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Удаление позиции",
            description = "Берет текущего авторизированного пользователя и создает позицию для него"
    )
    public void deletePosition(@PathVariable @Parameter(description = "Идентификатор позиции") Long id){
        positionService.delete(id);
    }
}
