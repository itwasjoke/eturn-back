package com.eturn.eturn.controller;

import com.eturn.eturn.domain.Group;
import com.eturn.eturn.domain.Member;
import com.eturn.eturn.domain.Turn;
import com.eturn.eturn.domain.User;
import com.eturn.eturn.repo.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.function.Consumer;

@RestController
@RequestMapping("group")
public class GrpController {
    private final GrpRepo grpRepo;
    private final MmbRepo mmbRepo;
    private final PosRepo posRepo;
    private final UsrRepo usrRepo;
    private final AGroupsRepo aGroupsRepo;
    private final TrnRepo trnRepo;

    public GrpController(GrpRepo grpRepo, MmbRepo mmbRepo, PosRepo posRepo,
                         UsrRepo usrRepo, AGroupsRepo aGroupsRepo, TrnRepo trnRepo) {
        this.grpRepo = grpRepo;
        this.mmbRepo = mmbRepo;
        this.posRepo = posRepo;
        this.usrRepo = usrRepo;
        this.aGroupsRepo = aGroupsRepo;
        this.trnRepo = trnRepo;
    }


    @GetMapping
    public List<Group> getGroupList(){
        return grpRepo.findAll();
    }

    @PostMapping
    public Group create(@RequestBody Group group){
        //
        //Ниже проверяем: есть ли такая группа уже в репозитории
        //
        if (grpRepo.existsByNumber(group.getNumber())) {return null;}
        return grpRepo.save(group);


    }

    @DeleteMapping("{number}")
    public void delete(@PathVariable("number") int number) {
        Group group = grpRepo.getByNumber(number);
        List<User> users= usrRepo.findByIdGroup(group.getId());
        users.forEach(new Consumer<User>(){
            @Override
            public void accept(User user) {
                List<Member> members= mmbRepo.findByIdUser(user.getId());
                if (!members.isEmpty())
                {
                    members.forEach(new Consumer<Member>() {
                        @Override
                        public void accept(Member member){
                            List<Turn> currentTurns= trnRepo.findByIdUser(member.getIdUser());
                            currentTurns.forEach(new Consumer<Turn>() {
                                @Override
                                public void accept(Turn turn) {
                                    mmbRepo.deleteByIdTurn(turn.getId());
                                    posRepo.deleteByIdTurn((turn.getId()));
                                    aGroupsRepo.deleteByIdTurn(turn.getId());
                                }
                            });
                            trnRepo.deleteByIdUser(member.getIdUser());
                            posRepo.deleteByIdUser(member.getIdUser());
                            mmbRepo.delete(member);
                        }
                    });
                    aGroupsRepo.deleteByIdGroup(group.getId());
                    usrRepo.deleteByIdGroup(group.getId());

                }
            }
        });


        grpRepo.delete(group);

    }
}
