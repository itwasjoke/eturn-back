package com.eturn.eturn.controller;


import com.eturn.eturn.domain.Turn;
import com.eturn.eturn.domain.User;
import com.eturn.eturn.repo.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.function.Consumer;

@RestController
@RequestMapping("user")
public class UsrController {
    private final UsrRepo usrRepo;
    private final MmbRepo mmbRepo;
    private final PosRepo posRepo;
    private final TrnRepo trnRepo;
    private final AGroupsRepo aGroupsRepo;

    public UsrController(UsrRepo usrRepo, MmbRepo mmbRepo, PosRepo posRepo,
                         TrnRepo trnRepo, AGroupsRepo aGroupsRepo) {

        this.usrRepo = usrRepo;
        this.mmbRepo = mmbRepo;
        this.posRepo = posRepo;
        this.trnRepo = trnRepo;
        this.aGroupsRepo = aGroupsRepo;
    }

    @GetMapping
    public List<User> getUserList(){return usrRepo.findAll();}

    @PostMapping
    public User create(@RequestBody User user){
        return usrRepo.save(user);
    }

    @DeleteMapping("{id}")
    public void delete(@PathVariable("id") User user){
        mmbRepo.deleteByIdUser(user.getId());
        posRepo.deleteByIdUser(user.getId());
        List<Turn> turns= trnRepo.findByIdUser(user.getId());
        turns.forEach(new Consumer<Turn>(){
            @Override
            public void accept(Turn turn){
                aGroupsRepo.deleteByIdTurn(turn.getId());
                mmbRepo.deleteByIdTurn(turn.getId());
                posRepo.deleteByIdTurn(turn.getId());
                trnRepo.delete(turn);
            }

        });


        usrRepo.delete(user);
    }



}
