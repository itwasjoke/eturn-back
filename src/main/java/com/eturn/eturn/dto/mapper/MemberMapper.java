package com.eturn.eturn.dto.mapper;

import com.eturn.eturn.dto.MemberDTO;
import com.eturn.eturn.entity.Member;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/** Маппер для участника */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface MemberMapper {
    @Mapping(target="id", source="member.id")
    @Mapping(target="userId", source="member.user.id")
    @Mapping(target="turnId", source="member.turn.id")
    @Mapping(target = "userName", source = "member.user.name")
    @Mapping(target = "group", source = "member.user.group.number")
    @Mapping(target = "access", source = "member.accessMember")
    @Mapping(target = "invited", source = "member.invited")
    @Mapping(target = "invitedForTurn", source = "member.invitedForTurn")
    MemberDTO memberToMemberDTO(Member member);

}
