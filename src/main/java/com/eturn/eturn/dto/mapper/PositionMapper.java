package com.eturn.eturn.dto.mapper;

import com.eturn.eturn.dto.PositionDTO;
import com.eturn.eturn.entity.Position;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/** Маппер для позиции */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface PositionMapper {
    @Mapping(target="id", source="position.id")
    @Mapping(target="name", source="position.user.name")
    @Mapping(target="group", source="position.groupName")
    @Mapping(target = "start", source = "position.start")
    PositionDTO positionToPositionDTO(Position position);
}
