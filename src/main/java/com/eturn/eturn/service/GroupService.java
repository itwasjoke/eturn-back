package com.eturn.eturn.service;

import com.eturn.eturn.dto.GroupDTO;
import com.eturn.eturn.entity.Group;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

public interface GroupService {

    Set<GroupDTO> getAllGroups();
    Long createGroup(GroupDTO group);
    Group getOneGroup(String number);
    Group getGroup(Long id);

    Set<Group> getSetGroups(Set<GroupDTO> groups);
}
