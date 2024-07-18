package com.eturn.eturn.service;

import com.eturn.eturn.dto.GroupDTO;
import com.eturn.eturn.entity.Faculty;
import com.eturn.eturn.entity.Group;

import java.util.Optional;

public interface GroupService {

    void createGroup(Group group);

    Group createGroup(GroupDTO groupDTO);

    Optional<Group> getGroup(String number);
    Group getGroup(Long id);

    Group getGroupForUser(String group, Faculty faculty);
}
