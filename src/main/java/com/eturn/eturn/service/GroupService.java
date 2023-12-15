package com.eturn.eturn.service;

import com.eturn.eturn.entity.Group;
import org.springframework.stereotype.Service;

import java.util.List;

public interface GroupService {
    void deleteGroup(int number);
    List<Group> getAllGroups();
    Group createGroup(Group group);
    Group getOneGroup(String number);
}
