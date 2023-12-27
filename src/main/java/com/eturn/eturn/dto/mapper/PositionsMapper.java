package com.eturn.eturn.dto.mapper;

import com.eturn.eturn.dto.PositionsDTO;
import com.eturn.eturn.entity.Position;
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
public interface PositionsMapper {
    @Mapping(target="id", source="position.id")
    @Mapping(target="name", source="position.user.name")
    @Mapping(target="group", source="position.user.idGroup")
    PositionsDTO positionToPositionDTO(Position position);

    @Mapping(target="id", source="position.id")
    @Mapping(target="user", source="userTemp")
    @Mapping(target="turn", source="turnTemp")
    @Mapping(target="started", source="startedTmp")
    @Mapping(target="number", source="numberTmp")
    Position positionDTOToPosition(PositionsDTO position, Turn turnTemp, User userTemp,
                                   boolean startedTmp,int numberTmp);
}
