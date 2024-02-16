package com.eturn.eturn.controller;

import com.eturn.eturn.dto.PositionDTO;
import com.eturn.eturn.dto.PositionMoreInfoDTO;
import com.eturn.eturn.service.PositionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value ="/position", produces = "application/json; charset=utf-8")
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

    @GetMapping(value = "/first")
    public PositionMoreInfoDTO getUserFirstPosition(@RequestParam Long turnId, @RequestParam Long userId){
        return positionService.getFirstUserPosition(turnId, userId);
    }

    @GetMapping ("/all/{idTurn}")
    public List<PositionDTO> getTurnPositions(@PathVariable Long idTurn,
                                              @RequestParam(defaultValue = "0") int page){
        return positionService.getPositonList(idTurn,page);
    }
    @PostMapping
    public PositionMoreInfoDTO createPosition(
                             @RequestParam Long idUser,
                             @RequestParam Long idTurn){
        return positionService.createPositionAndSave(idUser,idTurn);
    }

    @PutMapping("/rules")
    public void updateStatus(@RequestParam Long id,
                             @RequestParam boolean isStarted){
        positionService.update(id,isStarted);
    }
    @DeleteMapping("/{id}")
    public void deletePosition(@PathVariable Long id){
        positionService.delete(id);
    }
}
