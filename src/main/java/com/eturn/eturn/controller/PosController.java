package com.eturn.eturn.controller;

import com.eturn.eturn.client.PositionToClient;
import com.eturn.eturn.domain.*;
import com.eturn.eturn.repo.GrpRepo;
import com.eturn.eturn.repo.MmbRepo;
import com.eturn.eturn.repo.PosRepo;
import com.eturn.eturn.repo.UsrRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@RestController
@RequestMapping("position")
public class PosController {

    private final PosRepo posRepo;
    private final UsrRepo usrRepo;
    private final MmbRepo mmbRepo;

    private final GrpRepo grpRepo;
    @Autowired
    public PosController(PosRepo posRepo, UsrRepo usrRepo, MmbRepo mmbRepo, GrpRepo grpRepo) {
        this.posRepo = posRepo;
        this.usrRepo = usrRepo;
        this.mmbRepo = mmbRepo;
        this.grpRepo = grpRepo;
    }
    @GetMapping
    public List<Position> getPositionsList(){
        return posRepo.findAll();
    }
//    positions?id_user=1&id_turn=1
//    @GetMapping
//    public List<Position> getPositions(
//            @RequestParam(value = "id_turn", required = false) Long id_turn,
//            @RequestParam(value = "id_user", required = false) Long id_user){
//
//        return positionRepo.findByIdTurnAndIdUser(id_turn,id_user);
//    }
    @GetMapping("{id_turn}")
    public List<PositionToClient> getPositions(@PathVariable("id_turn") Long id_turn){
        List<Position> positions = posRepo.findByIdTurn(id_turn);
        List<PositionToClient> positionToClients = new ArrayList<>();
        positions.forEach(new Consumer<Position>() {
            @Override
            public void accept(Position position) {
                User user = usrRepo.getById(position.getIdUser());
                PositionToClient positionToClient = new PositionToClient();
                positionToClient.setId(position.getId());
                positionToClient.setName(user.getName());
                Group group = grpRepo.getById(user.getIdGroup());
                positionToClient.setNumberGroup(group.getNumber());
                positionToClients.add(positionToClient);
            }
        });
        return positionToClients;
    }


    @PostMapping
    public PositionToClient create(@RequestBody Position position){
        PositionToClient positionToClient= new PositionToClient();
        position.setCreationDate(LocalDateTime.now());
        posRepo.save(position);
        User user= usrRepo.getById(position.getIdUser());
        Group group = grpRepo.getById(user.getIdGroup());
        positionToClient.setId(position.getId());
        positionToClient.setNumberGroup(group.getNumber());
        positionToClient.setName(user.getName());
        return positionToClient;


    }

    @DeleteMapping("{id}")
    public void delete(
                        @PathVariable("id") Position position,
                        @RequestParam(value = "id_user", required = false) Long id_user) {

        if (Objects.equals(id_user, position.getIdUser()))
        {
            posRepo.delete(position);
            return;
        }
        Member member = mmbRepo.getByIdUserAndIdTurn(id_user, position.getIdTurn());
        if (member.getRoot()==2 || member.getRoot()==1)
        {
            posRepo.delete(position);
        }
    }
}
