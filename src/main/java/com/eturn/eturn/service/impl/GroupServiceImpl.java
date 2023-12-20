package com.eturn.eturn.service.impl;

import com.eturn.eturn.dto.GroupDTO;
import com.eturn.eturn.dto.mapper.GroupListMapper;
import com.eturn.eturn.dto.mapper.GroupMapper;
import com.eturn.eturn.entity.Group;
import com.eturn.eturn.exception.NotFoundException;
import com.eturn.eturn.repository.GroupRepository;
import com.eturn.eturn.service.GroupService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GroupServiceImpl implements GroupService {
    private final GroupRepository groupRepository;
    private final GroupMapper groupMapper;
    private final GroupListMapper groupListMapper;

    public GroupServiceImpl(GroupRepository groupRepository, GroupMapper groupMapper, GroupListMapper groupListMapper) {
        this.groupRepository = groupRepository;
        this.groupMapper = groupMapper;
        this.groupListMapper = groupListMapper;
    }

    @Override
    public List<GroupDTO> getAllGroups() {
        return groupListMapper.map(groupRepository.findAll());
    }

    @Override
    public Long createGroup(GroupDTO group) {
        if (!groupRepository.existsByNumber(group.name())){
            Group groupDb = groupMapper.DTOtoGroup(group);
            return groupRepository.save(groupDb).getId();
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

    @Override
    public Group getGroup(Long id) {
            Optional<Group> group = groupRepository.findById(id);
            if (group.isPresent()){
                return group.get();
            }
            else{
                throw new NotFoundException("Группа не найдена");
            }
    }
}
