package com.eturn.eturn.controller;

import com.eturn.eturn.entity.Group;
import com.eturn.eturn.entity.Member;
import com.eturn.eturn.entity.Turn;
import com.eturn.eturn.entity.User;
import com.eturn.eturn.repository.*;
import com.eturn.eturn.service.GroupService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/group")
public class GroupController {

    GroupService groupService;

    @GetMapping
    public List<Group> getGroupList(){
        return groupService.getAllGroups();
    }



//    @DeleteMapping("/{number}")
//    public void delete(@PathVariable int number) {
//        Group group = groupRepository.getByNumber(number);
//        List<User> users= userRepository.findByIdGroup(group.getId());
//        users.forEach(user -> {
//            List<Member> members= mmbRepo.findByIdUser(user.getId());
//            if (!members.isEmpty())
//            {
//                members.forEach(member -> {
//                    List<Turn> currentTurns= turnRepository.findByIdUser(member.getIdUser());
//                    currentTurns.forEach(turn -> {
//                        mmbRepo.deleteByIdTurn(turn.getId());
//                        positionRepository.deleteByIdTurn((turn.getId()));
//                        allowedGroupRepository.deleteByIdTurn(turn.getId());
//                    });
//                    turnRepository.deleteByIdUser(member.getIdUser());
//                    positionRepository.deleteByIdUser(member.getIdUser());
//                    mmbRepo.delete(member);
//                });
//                allowedGroupRepository.deleteByIdGroup(group.getId());
//                userRepository.deleteByIdGroup(group.getId());
//
//            }
//        });
//
//
//        groupRepository.delete(group);
//    }
}
