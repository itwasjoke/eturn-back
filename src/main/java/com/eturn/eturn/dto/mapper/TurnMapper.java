package com.eturn.eturn.dto.mapper;

import com.eturn.eturn.dto.MembersCountDTO;
import com.eturn.eturn.dto.TurnDTO;
import com.eturn.eturn.entity.Turn;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/** Маппер для очереди */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface TurnMapper {
    @Mapping(target = "creator",source = "turn.creator.name")
    @Mapping(target = "access", source = "access")
    @Mapping(target = "accessType", source = "accessType")
    @Mapping(target = "invitedForTurn", source = "invitedForTurn")
    @Mapping(target = "invitedModerator", source = "invitedModerator")
    @Mapping(target = "existsInvited", source = "existsInvited")
    @Mapping(target = "membersCount", source = "membersCount")
    @Mapping(target = "accessElements", source = "accessElements")
    @Mapping(target ="countUsers", source = "countUsers")
    TurnDTO turnToTurnDTO(
            Turn turn,
            String access,
            String accessType,
            String invitedForTurn,
            boolean invitedModerator,
            boolean existsInvited,
            MembersCountDTO membersCount,
            List<Long> accessElements,
            long countUsers
    );
}
