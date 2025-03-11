package com.eturn.eturn.dto.mapper;

import com.eturn.eturn.dto.FeedbackDTO;
import com.eturn.eturn.entity.Feedback;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface FeedbackMapper {
    @Mapping(target="name", source="feedback.user.name")
    @Mapping(target="group", source="feedback.user.group.number")
    @Mapping(target="userId", source = "feedback.user.id")
    FeedbackDTO feedbackToDTO(Feedback feedback);
}
