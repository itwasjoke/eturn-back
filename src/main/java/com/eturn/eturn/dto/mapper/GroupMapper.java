package com.eturn.eturn.dto.mapper;

import com.eturn.eturn.dto.GroupDTO;
import com.eturn.eturn.entity.Group;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface GroupMapper {
    @Mapping(target = "turns", ignore = true)
    @Mapping(target = "number", source = "dto.number")
    Group DTOtoGroup(GroupDTO dto);
}
