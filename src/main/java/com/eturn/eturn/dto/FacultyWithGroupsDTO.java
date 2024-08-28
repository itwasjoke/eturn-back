package com.eturn.eturn.dto;

import java.util.List;

public record FacultyWithGroupsDTO(
        long id,
        String name,
        List<GroupDTO> groups
) {
}
