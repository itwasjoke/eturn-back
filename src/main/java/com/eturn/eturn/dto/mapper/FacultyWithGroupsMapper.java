package com.eturn.eturn.dto.mapper;

import com.eturn.eturn.dto.FacultyWithGroupsDTO;
import com.eturn.eturn.entity.Faculty;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = GroupListMapper.class,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface FacultyWithGroupsMapper {
    @Mapping(target="id", source="faculty.id")
    @Mapping(target="name", source="faculty.name")
    @Mapping(target="groups", source="faculty.groups")
    FacultyWithGroupsDTO FacultyToFacultyWithGroupsDTO(Faculty faculty);
}
