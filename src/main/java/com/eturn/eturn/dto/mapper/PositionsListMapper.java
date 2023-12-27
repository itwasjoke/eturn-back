package com.eturn.eturn.dto.mapper;

import com.eturn.eturn.dto.PositionsDTO;
import com.eturn.eturn.entity.Position;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = PositionsMapper.class,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface PositionsListMapper {
    List<PositionsDTO> map(List<Position> position);
    List<PositionsDTO> map(Page<Position> position);
}
