package com.eturn.eturn.controller;

import com.eturn.eturn.dto.PositionsDTO;
import com.eturn.eturn.entity.Position;
import com.eturn.eturn.entity.Turn;
import com.eturn.eturn.service.PositionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value ="/positions", produces = "application/json; charset=utf-8")
public class positionsController {
     private final PositionService positionService;

    public positionsController(PositionService positionService) {
        this.positionService = positionService;
    }
    @GetMapping("{id}")
    public PositionsDTO getPosition(@PathVariable Long id){
        return positionService.getPositionById(id);
    }
    @GetMapping ("/all/{idTurn}")
    public List<PositionsDTO> getTurnPositions(@PathVariable Long idTurn,
                                               @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "3") int size){
        return positionService.getPositonList(idTurn,page,size);
    }
    @PostMapping
    public void createPosition(
                             @RequestParam Long idUser,
                             @RequestParam Long idTurn){
        positionService.createPositionAndSave(idUser,idTurn);
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
