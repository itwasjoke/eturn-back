package com.eturn.eturn.controller;

import com.eturn.eturn.entity.Turn;
import com.eturn.eturn.service.TurnService;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/turn")
public class TurnController {

    TurnService turnService;

    @GetMapping("{idTurn}")
    public Turn getTurn(@PathVariable Long idTurn) {
        return turnService.getTurn(idTurn);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Turn> getUserTurns( // TODO вынести в DTO ответы для frontend
        @RequestParam Long idUser,
        @RequestParam String type,
        @RequestParam String access,
        @RequestParam(required = false) String numberGroup,
        @RequestParam(required = false) String courseId,
        @RequestParam(required = false) String facultyId
    ) {
        Map<String, String> params = new HashMap<>(); // TODO Эту часть можно удалить и передавать переменные
        params.put("Type", type); // TODO аргументы маленькими буквами
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
        return turnService.getUserTurns(idUser, params);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Turn create(@RequestBody Turn turn) { // TODO для RequestBody создаем Dto и лучше сделать void/Long возвращать id очереди
        return turnService.createTurn(turn);
    }

    @PutMapping("{turnBd}") // TODO лучше здесь начинать с /{turnId}
    public Turn update(
        @PathVariable Turn turnBd,
        @RequestBody Turn turnNew,
        @RequestParam Long idUser // TODO либо param либо path variable
    ) {
        return turnService.updateTurn(idUser, turnBd, turnNew);
    }

    @PutMapping("{turn}/position") // TODO аналогично
    public void addPosition(
        @PathVariable Turn turn,
        @RequestParam Long idUser
    ) {
        turnService.addPositionToTurn(idUser, turn);
    }

    @DeleteMapping("{turn}") // TODO /turns/{turnId} уточнить во мн или в ед числе
    public void delete( // TODO deleteTurnWithPosition
        @PathVariable Turn turn,
        @RequestParam Long idUser,
        @RequestParam(required = false) Long idPosition
    ) {
        if (idPosition == null) {
            turnService.deleteTurn(idUser, turn);
        } else {
            turnService.deletePosition(idPosition, idUser, turn);
        }
    }

    // TODO deletePosition
    // TODO /turns/{turnId}/positions/{positionId} @DeleteMapping
}
