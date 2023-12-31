package com.eturn.eturn.controller;

import com.eturn.eturn.dto.GroupDTO;
import com.eturn.eturn.entity.Group;
import com.eturn.eturn.service.GroupService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping(value = "/group", produces = "application/json; charset=utf-8")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @GetMapping
    public Set<GroupDTO> getGroupList(){
        return groupService.getAllGroups();
    }

    @GetMapping(value = "/{number}")
    public GroupDTO getGroup(@PathVariable String number){
        return groupService.getOneGroupDTO(number);
    }

    @PostMapping
    public Long createGroup(@RequestBody GroupDTO groupDTO){
        return groupService.createGroup(groupDTO);
    }
}
