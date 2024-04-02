package com.eturn.eturn.dto.mapper;

import com.eturn.eturn.dto.MemberDTO;
import com.eturn.eturn.entity.Member;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

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
    @Mapping(target = "access", source = "member.accessMemberEnum")
    MemberDTO memberToMemberDTO(Member member);

}