package com.eturn.eturn.controller;

import com.eturn.eturn.domain.AllowGroup;
import com.eturn.eturn.domain.Group;
import com.eturn.eturn.domain.Member;
import com.eturn.eturn.domain.User;
import com.eturn.eturn.repo.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.function.Consumer;

@RestController
@RequestMapping("allowGroup")
public class AGroupsController {

    private final AGroupsRepo aGroupsRepo;
    private final GrpRepo grpRepo;
    private final MmbRepo mmbRepo;
    private final PosRepo posRepo;
    private final UsrRepo usrRepo;


    public AGroupsController(AGroupsRepo allowGroupRepo, GrpRepo grpRepo,
                             MmbRepo mmbRepo, PosRepo posRepo, UsrRepo usrRepo) {
        this.aGroupsRepo = allowGroupRepo;
        this.grpRepo = grpRepo;
        this.mmbRepo = mmbRepo;
        this.posRepo = posRepo;
        this.usrRepo = usrRepo;
    }

//
//    @GetMapping
//    public List<AllowGroup> getAllowGroupList(){
//        return allowGroupsRepo.findAll();
//    }

    @GetMapping()
    public List<AllowGroup> getAllowGroup(@RequestParam(value = "id_turn", required = false) Long id_turn){
        return aGroupsRepo.findByIdTurn(id_turn);
    }

    @PostMapping
    public List<AllowGroup> create(
            @RequestBody List<AllowGroup> allowGroups,
            @RequestParam(value = "id_user", required = false) Long id_user){
        Member member = mmbRepo.getByIdUserAndIdTurn(id_user,allowGroups.get(0).getIdTurn());
        if (member.getRoot()==2) {
            allowGroups.forEach(new Consumer<AllowGroup>() {

                @Override
                public void accept(AllowGroup allowGroup) {


                    Long id_group_illusion = allowGroup.getIdGroup();
                    int number = id_group_illusion.intValue();
                    if (grpRepo.existsByNumber(number)) {
                        Group group = grpRepo.getByNumber(number);
                        allowGroup.setIdGroup(group.getId());
                        aGroupsRepo.save(allowGroup);
                    } else {
                        Group group = new Group();
                        group.setNumber(number);
                        Group createdGroup = grpRepo.save(group);
                        allowGroup.setIdGroup(createdGroup.getId());
                        aGroupsRepo.save(allowGroup);
                    }
                }

            });
        }
        return allowGroups;
    }

    @DeleteMapping("{id_allow}")
    public void delete(
            @PathVariable("id_allow") AllowGroup allowGroup,
            @RequestParam(value = "id_user", required = false) Long id_user) {
        Member member = mmbRepo.getByIdUserAndIdTurn(id_user,allowGroup.getIdTurn());
        if (member.getRoot()==2){
            List<User> users= usrRepo.findByIdGroup(allowGroup.getIdGroup());
            users.forEach(new Consumer<User>() {
                @Override
                public void accept(User user) {
                    Member member= mmbRepo.getByIdUserAndIdTurn(user.getId(), allowGroup.getIdTurn());
                    posRepo.deleteByIdUserAndIdTurn(user.getId(), allowGroup.getIdTurn());
                    mmbRepo.delete(member);
                }
            });
            aGroupsRepo.delete(allowGroup);
        }


    }



}
