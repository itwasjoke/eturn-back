package com.eturn.eturn.dto;

import com.eturn.eturn.entity.*;
import com.eturn.eturn.enums.AccessTurnEnum;
import com.eturn.eturn.enums.TurnEnum;

import java.util.Set;

public record TurnMoreInfoDTO(
        Long id,
        String name,
        String description,
        Long creator,
        TurnEnum turnType,
        AccessTurnEnum turnAccess,
        Set<GroupDTO> allowedGroups,
        Set<Faculty> allowedFaculties,
        Set<Department> allowedDepartments,
        Set<Course> allowedCourses
) {
    public TurnMoreInfoDTO(Long id, String name, String description, Long creator, TurnEnum turnType, AccessTurnEnum turnAccess, Set<GroupDTO> allowedGroups, Set<Faculty> allowedFaculties, Set<Department> allowedDepartments, Set<Course> allowedCourses) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.creator = creator;
        this.turnType = turnType;
        this.turnAccess = turnAccess;
        this.allowedGroups = allowedGroups;
        this.allowedFaculties = allowedFaculties;
        this.allowedDepartments = allowedDepartments;
        this.allowedCourses = allowedCourses;
    }
}
