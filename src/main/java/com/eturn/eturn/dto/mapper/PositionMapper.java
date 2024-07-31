package com.eturn.eturn.dto.mapper;

import com.eturn.eturn.dto.PositionDTO;
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
public interface PositionMapper {
    @Mapping(target="id", source="position.id")
    @Mapping(target="name", source="position.user.name")
    @Mapping(target="group", source="position.groupName")
    @Mapping(target = "userId", source = "position.user.id")
    @Mapping(target = "start", source = "position.start")
    PositionDTO positionToPositionDTO(Position position);

    @Mapping(target="id", source="position.id")
    @Mapping(target="user", source="userTemp")
    @Mapping(target="turn", source="turnTemp")
    @Mapping(target="start", source="startedTmp")
    @Mapping(target="number", source="numberTmp")
    Position positionDTOToPosition(PositionDTO position, Turn turnTemp, User userTemp,
                                   Boolean startedTmp, int numberTmp);
}
