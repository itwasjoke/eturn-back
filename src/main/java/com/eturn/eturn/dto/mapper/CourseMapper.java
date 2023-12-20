package com.eturn.eturn.dto.mapper;

import com.eturn.eturn.dto.CourseDTO;
import com.eturn.eturn.entity.Course;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface CourseMapper {

    @Mapping(target = "turns", ignore = true)
    Course DTOtoCourse(CourseDTO dto);

    CourseDTO courseToDTO(Course course);
}
