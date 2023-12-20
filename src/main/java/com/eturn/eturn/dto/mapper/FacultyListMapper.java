package com.eturn.eturn.dto.mapper;

import com.eturn.eturn.dto.FacultyDTO;
import com.eturn.eturn.entity.Faculty;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = FacultyMapper.class,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface FacultyListMapper {
    List<FacultyDTO> map(List<Faculty> list);
}
