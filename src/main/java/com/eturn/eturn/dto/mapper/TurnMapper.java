package com.eturn.eturn.dto.mapper;

import com.eturn.eturn.dto.TurnDTO;
import com.eturn.eturn.entity.Turn;
import com.eturn.eturn.entity.User;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.stereotype.Component;

import java.util.List;
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface TurnMapper {
    @Mapping(target = "creator",source = "turn.creator.name")
    @Mapping(target = "userId", source = "turn.creator.id")
    TurnDTO turnToTurnDTO(Turn turn);
}
