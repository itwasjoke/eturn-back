package com.eturn.eturn.dto.mapper;

import com.eturn.eturn.dto.FeedbackDTO;
import com.eturn.eturn.entity.Feedback;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = FeedbackMapper.class,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface FeedbackListMapper {
    List<FeedbackDTO> mapFeedback(List<Feedback> list);
}
