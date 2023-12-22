package com.eturn.eturn.dto.mapper;

import com.eturn.eturn.dto.GroupDTO;
import com.eturn.eturn.entity.Group;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.Set;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = GroupMapper.class,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface GroupListMapper {
    Set<GroupDTO> map(Set<Group> groups);
}
