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
    public Group createGroup(GroupDTO group) {
        if (!groupRepository.existsByNumber(group.name())){
            Group groupDb = groupMapper.DTOtoGroup(group);
            return groupRepository.save(groupDb);
        }
        else{
            throw new AlreadyExistGroupException("group already exist");
        }
    }

    @Override
    public void createGroup(Group group) {
        groupRepository.save(group);
    }

    @Override
    public Optional<Group> getGroup(String number) {
        return groupRepository.getGroupByNumber(number);
    }

    @Override
    public Group getGroup(Long id) {
            Optional<Group> group = groupRepository.findById(id);
            if (group.isPresent()){
                return group.get();
            }
            else{
                throw new NotFoundGroupException("Cannot find group by ID.");
            }
    }

    @Override
    public Group getGroupForUser(String group, Faculty faculty) {
        Optional<Group> g = getGroup(group);
        if (g.isPresent()){
            return g.get();
        }
        else {

            GroupDTO newGroup = new GroupDTO(null, group, faculty.getId());
            return createGroup(newGroup);
        }
    }
}
