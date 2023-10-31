package com.eturn.eturn.controller;


import com.eturn.eturn.domain.Group;
import com.eturn.eturn.domain.Member;
import com.eturn.eturn.domain.User;
import com.eturn.eturn.repo.GrpRepo;
import com.eturn.eturn.repo.MmbRepo;
import com.eturn.eturn.repo.PosRepo;
import com.eturn.eturn.repo.UsrRepo;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@RestController
@RequestMapping("member")
public class MmbController {

    private final MmbRepo mmbRepo;
    private final PosRepo positionRepo;

    private final UsrRepo usrRepo;
    private final GrpRepo grpRepo;


    public MmbController(MmbRepo mmbRepo, PosRepo posRepo, UsrRepo usrRepo, GrpRepo grpRepo) {
        this.mmbRepo = mmbRepo;
        this.positionRepo= posRepo;
        this.usrRepo = usrRepo;
        this.grpRepo = grpRepo;
    }

    @GetMapping
    public List<User> getMemberList(
            @RequestParam(value = "id_turn", required = false) Long id_turn,
            @RequestParam(value = "admin", required = false)boolean isAdmin){
        List<Member> members;
        if (isAdmin){
            members = mmbRepo.findByIdTurnAndRootNot(id_turn, 0);
        }
        else{
            members = mmbRepo.findByIdTurnAndRoot(id_turn, 0);
        }
        List<User> users = new ArrayList<User>();
        members.forEach(new Consumer<Member>() {
            @Override
            public void accept(Member member) {
                User user = usrRepo.getById(member.getIdUser());
                Group group = grpRepo.getById(user.getIdGroup());
                int numberInt = group.getNumber();
                Long number = (long) numberInt;
                user.setIdGroup(number);
                users.add(user);
            }
        });
        return users;
    }

    @GetMapping("root")
    public Member checkRootUser(
            @RequestParam(value = "id_user", required = false) Long id_user,
            @RequestParam(value = "id_turn", required = false) Long id_turn){
        return mmbRepo.getByIdUserAndIdTurn(id_user, id_turn);
    }

    @PostMapping
    public Member create(@RequestBody Member member){
        member.setRoot(0);
        return mmbRepo.save(member);
    }
    @PutMapping()
    public Member update(
            @RequestBody int root,
            @RequestParam(value = "id_user", required = false) Long id_user_for,
            @RequestParam(value = "id_turn", required = false) Long id_turn,
            @RequestParam(value = "id_user_change", required = false) Long id_user){
        Member admin = mmbRepo.getByIdUserAndIdTurn(id_user_for,id_turn);
        Member memberFromDb = mmbRepo.getByIdUserAndIdTurn(id_user,id_turn);
        if (admin.getRoot()==2){
            if (root==0 || root==1) {
                memberFromDb.setRoot(root);
                return memberFromDb;
            }
        }
        return memberFromDb;
    }



    @DeleteMapping()
    public void delete(@RequestParam(value = "id_turn", required = false) Long id_turn,
                       @RequestParam(value = "id_user_delete", required = false) Long id_user_delete,
                       @RequestParam(value = "id_user", required = false) Long id_user) {
        if (id_user == id_user_delete)
        {
            positionRepo.deleteByIdUserAndIdTurn(id_user_delete, id_turn);
            return;
        }
        Member member= mmbRepo.getByIdUserAndIdTurn(id_user,id_turn);
        if (member.getRoot()==2 ||member.getRoot()==1) {

            positionRepo.deleteByIdUserAndIdTurn(member.getIdUser(), member.getIdTurn());
            mmbRepo.delete(member);
        }

    }




}
