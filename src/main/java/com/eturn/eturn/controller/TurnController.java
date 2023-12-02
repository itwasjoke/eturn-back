package com.eturn.eturn.controller;

import com.eturn.eturn.entity.Member;
import com.eturn.eturn.entity.Turn;
import com.eturn.eturn.service.TurnService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/turn")
public class TurnController {

    TurnService turnService;

    @GetMapping("{idTurn}")
    public Turn getTurn(@PathVariable Long idTurn){
        return turnService.getTurn(idTurn);
    }
    @GetMapping
    public List<Turn> getUserTurns(
            @RequestParam Long idUser,
            @RequestParam String type,
            @RequestParam String access,
            @RequestParam(required = false) String numberGroup,
            @RequestParam(required = false) String courseId,
            @RequestParam(required = false) String facultyId
            ){
        Map<String,String> params = new HashMap<>();
        params.put("Type", type);
        params.put("Access", access);

        if (numberGroup!=null){
            params.put("Group",numberGroup);
        }
        if (courseId!=null){
            params.put("Course", courseId);
        }
        if (facultyId!=null){
            params.put("Faculty", facultyId);
        }
        return turnService.getUserTurns(idUser,params);

    }

    @PostMapping
    public Turn create(@RequestBody Turn turn){
        return turnService.createTurn(turn);
    }

    @PutMapping("{turnBd}")
    public Turn update(
            @PathVariable Turn turnBd,
            @RequestBody Turn turnNew,
            @RequestParam Long idUser
    ){
        return turnService.updateTurn(idUser, turnBd, turnNew);
    }

    @PutMapping("{turn}/position")
    public void addPosition(
            @PathVariable Turn turn,
            @RequestParam Long idUser
    ){
        turnService.addPositionToTurn(idUser, turn);
    }

    @DeleteMapping("{turn}")
    public void delete(
            @PathVariable Turn turn,
            @RequestParam Long idUser,
            @RequestParam(required = false) Long idPosition
    ){
        if (idPosition==null){
            turnService.deleteTurn(idUser, turn);
        }
        else {
            turnService.deletePosition(idPosition, idUser, turn);
        }

    }


}
