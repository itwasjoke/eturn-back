package com.eturn.eturn.dto.mapper;

import com.eturn.eturn.dto.CourseDTO;
import com.eturn.eturn.entity.Course;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = CourseMapper.class,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface CourseListMapper {
    List<CourseDTO> map(List<Course> courses);
}
