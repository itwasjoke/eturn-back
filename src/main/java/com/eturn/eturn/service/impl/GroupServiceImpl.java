package com.eturn.eturn.service.impl;

import com.eturn.eturn.entity.Group;
import com.eturn.eturn.entity.Member;
import com.eturn.eturn.entity.Turn;
import com.eturn.eturn.entity.User;
import com.eturn.eturn.repository.GroupRepository;
import com.eturn.eturn.service.GroupService;
import com.eturn.eturn.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GroupServiceImpl implements GroupService {
    GroupRepository groupRepository;

    @Override
    public void deleteGroup(int number) {

    }
    @Override
    public List<Group> getAllGroups() {
        return groupRepository.findAll();
    }

    @Override
    public Group createGroup(Group group) {
        if (!groupRepository.existsByNumber(group.getNumber())){
            return groupRepository.save(group);
        }
        else{
            return null;
        }
    }

    @Override
    public Group getOneGroup(String number) {
        return groupRepository.getByNumber(number);
    }
}
