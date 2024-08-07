package com.eturn.eturn.dto.mapper;

import com.eturn.eturn.dto.PositionDTO;
import com.eturn.eturn.dto.PositionMoreInfoDTO;
import com.eturn.eturn.entity.Position;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface PositionMoreInfoMapper {
    @Mapping(target="id", source="position.id")
    @Mapping(target="name", source="position.user.name")
    @Mapping(target="group", source="position.groupName")
    @Mapping(target = "userId", source = "position.user.id")
    @Mapping(target = "start", source = "position.start")
    @Mapping(target = "dateEnd", source = "position.dateEnd")
    @Mapping(target = "difference", source = "dif")
    @Mapping(target = "isLast", ignore = true)
    PositionMoreInfoDTO positionMoreInfoToPositionDTO(Position position, int dif);
    @Mapping(target="id", source="position.id")
    @Mapping(target="name", source="position.user.name")
    @Mapping(target="group", source="position.groupName")
    @Mapping(target = "userId", source = "position.user.id")
    @Mapping(target = "start", source = "position.start")
    @Mapping(target = "dateEnd", source = "position.dateEnd")
    @Mapping(target = "difference", source = "dif")
    @Mapping(target = "isLast", source = "isLast")
    PositionMoreInfoDTO positionMoreUserToPositionDTO(Position position, int dif, boolean isLast);
}
