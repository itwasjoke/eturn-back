package com.eturn.eturn.controller;

import com.eturn.eturn.domain.AllowGroup;
import com.eturn.eturn.domain.Member;
import com.eturn.eturn.domain.Turn;
import com.eturn.eturn.domain.User;
import com.eturn.eturn.repo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.function.Consumer;

@RestController
@RequestMapping("turn")
public class TrnController {

    private final TrnRepo trnRepo;
    private final MmbRepo mmbRepo;

    private final UsrRepo usrRepo;

    private final PosRepo posRepo;

    private final AGroupsRepo aGroupsRepo;

    @Autowired
    public TrnController(TrnRepo trnRepo, MmbRepo mmbRepo, UsrRepo usrRepo, PosRepo posRepo, AGroupsRepo aGroupsRepo)
    {
        this.trnRepo = trnRepo;
        this.mmbRepo = mmbRepo;
        this.usrRepo = usrRepo;
        this.posRepo = posRepo;
        this.aGroupsRepo = aGroupsRepo;
    }

    @GetMapping
    public List<Turn> getTurnsList(Model model){return trnRepo.findAll();}

    @GetMapping("{id_turn}")
    public Turn getTurn(@PathVariable("id_turn") Long id_turn){
        return trnRepo.getById(id_turn);
    }
    @GetMapping("yours")
    public List<Turn> getYourTurns(@RequestParam(value = "id_user", required = false) Long id_user){
        List<Member> members = mmbRepo.findByIdUser(id_user);
        if (members.isEmpty()) return null;
        else{
            List<Turn> turns = new ArrayList<Turn>();
            members.forEach(new Consumer<Member>() {
                @Override
                public void accept(Member member) {
                    Long id_turn = member.getIdTurn();
                    Turn currentTurn;
                    currentTurn = trnRepo.getById(id_turn);
                    User creator = usrRepo.getById(currentTurn.getIdUser());
                    currentTurn.setNameCreator(creator.getName());
                    turns.add(currentTurn);
                }
            });
            return turns;
        }

    }

    @GetMapping("allow")
    public List<Turn> getAllowTurns(@RequestParam(value = "id_user", required = false) Long id_user){
        User currentUser = usrRepo.getById(id_user);
        List<AllowGroup> allowGroups = aGroupsRepo.findByIdGroup(currentUser.getIdGroup());
        List<Turn> turns = new ArrayList<Turn>();
        allowGroups.forEach(new Consumer<AllowGroup>() {
            @Override
            public void accept(AllowGroup allowGroup) {
                if (!mmbRepo.existsByIdUserAndIdTurn(id_user, allowGroup.getIdTurn())){

                Long id_turn = allowGroup.getIdTurn();
                Turn currentTurn;
                currentTurn = trnRepo.getById(id_turn);
                User creator = usrRepo.getById(currentTurn.getIdUser());
                currentTurn.setNameCreator(creator.getName());
                turns.add(currentTurn);

                }
            }
        });
        return turns;
    }

    @PostMapping
    public Turn create(@RequestBody Turn turn){
        if (turn.getDescription().length()<=255 && turn.getName().length()<=50) {
            Turn createdTurn = trnRepo.save(turn);

            Long idTurn = createdTurn.getId();
            Long idUser = createdTurn.getIdUser();
            Member memberCreator = new Member();
            memberCreator.setRoot(2);
            memberCreator.setIdTurn(idTurn);
            memberCreator.setIdUser(idUser);
            mmbRepo.save(memberCreator);
            return createdTurn;
        }
        return turn;
    }

    @PutMapping("{id_turn}")
    public Turn update(
            @PathVariable("id_turn") Turn turnFromDb,
            @RequestBody Turn turn,
            @RequestParam(value = "id_user", required = false) Long id_user
    ){
        Member member = mmbRepo.getByIdUserAndIdTurn(id_user,turnFromDb.getId());
        if (member.getRoot()==2 || member.getRoot()==1){
            BeanUtils.copyProperties(turn, turnFromDb,"id");
            return turnFromDb;
        } else return turn;
    }

    @DeleteMapping("{id}")
    public void delete(
            @PathVariable("id") Turn turn,
            @RequestParam(value = "id_user", required = false) Long id_user)
    {
        if (Objects.equals(id_user, turn.getIdUser())){
            mmbRepo.deleteByIdTurn(turn.getId());
            posRepo.deleteByIdTurn((turn.getId()));
            aGroupsRepo.deleteByIdTurn(turn.getId());
            trnRepo.delete(turn);
        }
    }


}
