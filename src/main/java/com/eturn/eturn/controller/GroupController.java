package com.eturn.eturn.controller;

import com.eturn.eturn.dto.GroupDTO;
import com.eturn.eturn.entity.Group;
import com.eturn.eturn.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping(value = "/group", produces = "application/json; charset=utf-8")
@Tag(name = "Группы", description = "Обновление списка групп")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

//    @GetMapping
////    @PreAuthorize("hasRole('EMPLOYEE')")
//    public Set<GroupDTO> getGroupList(){
//        return groupService.getAllGroups();
//    }

    @GetMapping(value = "/{number}")
//    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(
            summary = "Получение группы",
            description = "По номеру находит объект с группой"
    )
    public GroupDTO getGroup(@PathVariable @Parameter(description = "Номер группы из 4 цифр") String number){
        return groupService.getOneGroupDTO(number);
    }

    @PostMapping
//    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(
            summary = "Создание группы",
            description = "Создает новую группу"
    )
    public Long createGroup(@RequestBody GroupDTO groupDTO){
        return groupService.createGroup(groupDTO);
    }
}
