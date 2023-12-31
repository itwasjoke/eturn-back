package com.eturn.eturn.dto.mapper;

import com.eturn.eturn.dto.TurnDTO;
import com.eturn.eturn.entity.Turn;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = TurnMapper.class,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface TurnListMapper {
    List<TurnDTO> map(List<Turn> turns);
}
