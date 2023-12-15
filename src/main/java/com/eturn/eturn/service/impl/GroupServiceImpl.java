package com.eturn.eturn.service.impl;

import com.eturn.eturn.entity.Group;
import com.eturn.eturn.exception.NotFoundException;
import com.eturn.eturn.repository.GroupRepository;
import com.eturn.eturn.service.GroupService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GroupServiceImpl implements GroupService {
    private final GroupRepository groupRepository;

    public GroupServiceImpl(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

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
        if (groupRepository.existsByNumber(number)){
            return groupRepository.getByNumber(number);
        }
        else{
            throw new NotFoundException("Группа не найдена");
        }
    }
}
