package com.eturn.eturn.service.impl;

import com.eturn.eturn.dto.GroupDTO;
import com.eturn.eturn.dto.mapper.GroupListMapper;
import com.eturn.eturn.dto.mapper.GroupMapper;
import com.eturn.eturn.entity.Faculty;
import com.eturn.eturn.entity.Group;
import com.eturn.eturn.exception.group.AlreadyExistGroupException;
import com.eturn.eturn.exception.group.NotFoundGroupException;
import com.eturn.eturn.repository.GroupRepository;
import com.eturn.eturn.service.GroupService;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class GroupServiceImpl implements GroupService {
    private final GroupRepository groupRepository;
    private final GroupMapper groupMapper;

    public GroupServiceImpl(GroupRepository groupRepository, GroupMapper groupMapper) {
        this.groupRepository = groupRepository;
        this.groupMapper = groupMapper;
    }

    @Override
    public Optional<Group> getGroup(String number) {
        return groupRepository.getGroupByNumber(number);
    }

    @Override
    public void createOptionalGroup(Long id, String number, Integer course, Faculty faculty) {
        if (groupRepository.existsById(id)){
            Group groupExisted = groupRepository.getGroupById(id);
            if (groupExisted.getNumber()!=number || groupExisted.getCourse()!=course || groupExisted.getFaculty().getId()!=faculty.getId()){
                Group group = new Group();
                group.setId(id);
                group.setFaculty(faculty);
                group.setNumber(number);
                group.setCourse(course);
                groupRepository.save(group);
            }
        }
        else {
            Group group = new Group();
            group.setId(id);
            group.setFaculty(faculty);
            group.setNumber(number);
            group.setCourse(course);
            groupRepository.save(group);
        }
    }
}
