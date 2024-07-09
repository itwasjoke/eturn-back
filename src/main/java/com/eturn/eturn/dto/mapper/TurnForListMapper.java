package com.eturn.eturn.dto.mapper;

import com.eturn.eturn.dto.TurnDTO;
import com.eturn.eturn.dto.TurnForListDTO;
import com.eturn.eturn.entity.Turn;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface TurnForListMapper {
    @Mapping(target = "id",source = "turn.id")
    @Mapping(target = "name", source = "turn.name")
    @Mapping(target = "description",source = "turn.description")
    @Mapping(target = "userId", source = "turn.creator.id")
    @Mapping(target = "countUsers",source = "turn.countUsers")
    @Mapping(target = "allowedTime", source = "turn.allowedTime")
    @Mapping(target = "dateStart",source = "turn.dateStart")
    @Mapping(target = "dateEnd", source = "turn.dateEnd")
    @Mapping(target = "accessMember", source = "access")

    TurnForListDTO turnToTurnForListDTO(Turn turn, String access);
}
