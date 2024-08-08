package com.eturn.eturn.dto.mapper;

import com.eturn.eturn.dto.FacultyDTO;
import com.eturn.eturn.entity.Faculty;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface FacultyMapper {
    @Mapping(target="turns", ignore = true)
    Faculty DTOtoFaculty(FacultyDTO dto);
}
