package com.eturn.eturn.service;

import com.eturn.eturn.dto.GroupDTO;
import com.eturn.eturn.entity.Group;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface GroupService {

    Set<GroupDTO> getAllGroups(long id);
    Group createGroup(GroupDTO group);
    Group getOneGroup(String number);

    Optional<Group> getOneGroupOptional(String number);
    Group getGroup(Long id);
    GroupDTO getOneGroupDTO(String number);

    Set<Group> getSetGroups(Set<GroupDTO> groups);
}
