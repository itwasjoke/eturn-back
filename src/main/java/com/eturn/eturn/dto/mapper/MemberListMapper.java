package com.eturn.eturn.dto.mapper;

import com.eturn.eturn.dto.MemberDTO;
import com.eturn.eturn.entity.Member;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;

import java.util.List;

/** Маппер для списков участников */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = MemberMapper.class,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface MemberListMapper {
    List<MemberDTO> map(Page<Member> members);
    List<MemberDTO> mapMember(List<Member> members);
}
