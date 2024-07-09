package com.eturn.eturn.dto.mapper;

import com.eturn.eturn.dto.TurnCreatingDTO;
import com.eturn.eturn.entity.Turn;
import com.eturn.eturn.entity.User;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
       componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface TurnCreatingMapper {
    @Mapping(target = "creator", source = "user")
    @Mapping(target = "name", source = "dto.name")
    //@Mapping(target = "allowedGroups", source = "groups")
    @Mapping(target = "accessTurnType", source = "dto.turnAccess")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "positions", ignore = true)
    Turn turnMoreDTOToTurn(TurnCreatingDTO dto, User user);
}
