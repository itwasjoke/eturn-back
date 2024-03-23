package com.eturn.eturn.dto.mapper;

import com.eturn.eturn.dto.MemberDTO;
import com.eturn.eturn.dto.PositionDTO;
import com.eturn.eturn.entity.Member;
import com.eturn.eturn.entity.Position;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = MemberMapper.class,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface MemberListMapper {
    List<MemberDTO> map(List<Member> members);
}
