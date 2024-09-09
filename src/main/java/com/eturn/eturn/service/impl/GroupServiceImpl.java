package com.eturn.eturn.service.impl;

import com.eturn.eturn.entity.Faculty;
import com.eturn.eturn.entity.Group;
import com.eturn.eturn.repository.GroupRepository;
import com.eturn.eturn.service.GroupService;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GroupServiceImpl implements GroupService {
    private final GroupRepository groupRepository;
    public GroupServiceImpl(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }
    @Override
    public Optional<Group> getGroup(String number) {
        return groupRepository.getGroupByNumber(number);
    }
    @Override
    public void createOptionalGroup(Long id, String number, Integer course, Faculty faculty) {
        Optional<Group> group = groupRepository.getGroupByNumber(number);
        if (group.isPresent()){
            Group groupExisted = group.get();
            if (
                !Objects.equals(groupExisted.getCourse(), course)
                || !Objects.equals(groupExisted.getFaculty().getId(), faculty.getId())
            ) {
                groupExisted.setFaculty(faculty);
                groupExisted.setCourse(course);
                groupRepository.save(groupExisted);
            }
        }
        else {
            Group newGroup = new Group();
            newGroup.setFaculty(faculty);
            newGroup.setNumber(number);
            newGroup.setCourse(course);
            groupRepository.save(newGroup);
        }
    }
}
