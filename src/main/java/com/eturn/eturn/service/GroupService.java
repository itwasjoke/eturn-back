package com.eturn.eturn.service;

import com.eturn.eturn.dto.GroupDTO;
import com.eturn.eturn.entity.Group;
import org.springframework.stereotype.Service;

import java.util.List;

public interface GroupService {

    List<Group> getAllGroups();
    Long createGroup(GroupDTO group);
    Group getOneGroup(String number);
    Group getGroup(Long id);

}
