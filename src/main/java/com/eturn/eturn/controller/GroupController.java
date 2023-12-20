package com.eturn.eturn.controller;

import com.eturn.eturn.dto.GroupDTO;
import com.eturn.eturn.entity.Group;
import com.eturn.eturn.service.GroupService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/group")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @GetMapping
    public List<Group> getGroupList(){
        return groupService.getAllGroups();
    }

    @PostMapping
    public Long createGroup(GroupDTO groupDTO){
        return groupService.createGroup(groupDTO);
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
