package com.eturn.eturn.dto.mapper;

import com.eturn.eturn.dto.UserCreateDTO;
import com.eturn.eturn.dto.UserDTO;
import com.eturn.eturn.entity.*;
import com.eturn.eturn.enums.ApplicationType;
import com.eturn.eturn.enums.Role;
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
    @Mapping(target = "role", source = "role")
    UserDTO userToUserDTO(User user, String faculty, String group, String role);

    @Mapping(target = "role", source = "role")
    @Mapping(target = "applicationType", source = "type")
    User userCreateDTOtoUser(UserCreateDTO dto, Role role, ApplicationType type);
    }
