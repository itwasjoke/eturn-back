package com.eturn.eturn.dto.mapper;

import com.eturn.eturn.dto.TurnMoreInfoDTO;
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
public interface TurnMoreInfoMapper {
    @Mapping(target = "creator", source = "user")
    @Mapping(target = "name", source = "dto.name")
    @Mapping(target = "id", ignore = true)
    Turn turnMoreDTOToTurn(TurnMoreInfoDTO dto, User user);
    @Mapping(target = "creator", source = "turn.creator.id")
    TurnMoreInfoDTO turnToTurnMoreInfoDTO(Turn turn);

}
