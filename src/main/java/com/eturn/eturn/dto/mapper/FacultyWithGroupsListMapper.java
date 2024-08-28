package com.eturn.eturn.dto.mapper;

import com.eturn.eturn.dto.FacultyWithGroupsDTO;
import com.eturn.eturn.entity.Faculty;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = FacultyWithGroupsMapper.class,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface FacultyWithGroupsListMapper {
    List<FacultyWithGroupsDTO> map(List<Faculty> faculties);
}
