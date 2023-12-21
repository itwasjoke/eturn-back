package com.eturn.eturn.controller;

import com.eturn.eturn.dto.TurnDTO;
import com.eturn.eturn.dto.TurnMoreInfoDTO;
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
@RequestMapping(value ="/turn", produces = "application/json; charset=utf-8")
public class TurnController {

    private final TurnService turnService;

    public TurnController(TurnService turnService) {
        this.turnService = turnService;
    }

    @GetMapping("{idTurn}")
    public TurnDTO getTurn(@PathVariable long idTurn) {
        return turnService.getTurn(idTurn);
    }

    @GetMapping
    public List<TurnDTO> getUserTurns(
        @RequestParam long userId,
        @RequestParam String type,
        @RequestParam String access,
        @RequestParam(required = false) String numberGroup,
        @RequestParam(required = false) String courseId,
        @RequestParam(required = false) String facultyId
    ) {
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
        return turnService.getUserTurns(userId, params);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long create(@RequestBody TurnMoreInfoDTO turn) {
        return turnService.createTurn(turn);
    }

    @PutMapping()
    public void update(
        @RequestBody Turn turn,
        @RequestParam long idUser
    ) {
        turnService.updateTurn(idUser, turn);
    }

    @DeleteMapping()
    public void delete(
        @RequestParam long idUser,
        @RequestParam long idTurn
    ) {
        turnService.deleteTurn(idUser,idTurn);
    }
}
