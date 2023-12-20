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
    public List<GroupDTO> getGroupList(){
        return groupService.getAllGroups();
    }

    @PostMapping
    public Long createGroup(@RequestBody GroupDTO groupDTO){
        return groupService.createGroup(groupDTO);
    }
}
