package com.eturn.eturn.dto.mapper;

import com.eturn.eturn.dto.PositionDTO;
import com.eturn.eturn.entity.Position;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;

import java.util.List;

/** Маппер для списка позиций */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = PositionMapper.class,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface PositionListMapper {
    List<PositionDTO> map(Page<Position> position);
}
