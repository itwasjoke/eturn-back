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
        if (groupRepository.existsById(id)){
            Group groupExisted = groupRepository.getGroupById(id);
            if (
                    !Objects.equals(groupExisted.getNumber(), number)
                            || !Objects.equals(groupExisted.getCourse(), course)
                            || !Objects.equals(groupExisted.getFaculty().getId(), faculty.getId())
            ){
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
