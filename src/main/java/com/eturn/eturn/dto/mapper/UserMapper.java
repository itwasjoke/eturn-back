package com.eturn.eturn.dto.mapper;

import com.eturn.eturn.dto.UserDTO;
import com.eturn.eturn.entity.*;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface UserMapper {
    @Mapping(target = "faculty", source = "faculty")
    @Mapping(target = "group", source = "group")
    @Mapping(target = "course", source = "course")
    @Mapping(target = "department", source = "department")
    UserDTO userToUserDTO(User user, String faculty, String course, String department, String group);

    }
