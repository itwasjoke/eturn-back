package com.eturn.eturn.service;

import com.eturn.eturn.dto.GroupDTO;
import com.eturn.eturn.entity.Faculty;
import com.eturn.eturn.entity.Group;

import java.util.Optional;

public interface GroupService {

    void createOptionalGroup(Long id, String number, Integer course, Faculty faculty);
    Optional<Group> getGroup(String number);

}
