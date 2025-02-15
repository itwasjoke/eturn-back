package com.eturn.eturn.additionalService.member;

import com.eturn.eturn.dto.MemberListDTO;
import com.eturn.eturn.entity.Member;
import com.eturn.eturn.entity.Turn;
import com.eturn.eturn.entity.User;
import com.eturn.eturn.enums.AccessMember;
import com.eturn.eturn.enums.MemberListType;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface MemberRepositoryService {
    Page<Member> getMembersByAccess(Turn turn, AccessMember accessMember, int page);
    long getMemberCountByAccess(Turn turn, AccessMember accessMember);
    List<Member> getUnconfirmedMembers(Turn turn, AccessMember accessMember);
    long getUnconfirmedMemberCount(Turn turn, AccessMember accessMember);
    int getCountMembersWith(Turn turn, MemberListType memberListType);
    Optional<Member> getMemberWith(User user, Turn turn);

    MemberListDTO getUnconfirmedMemberList(
            String username,
            String type,
            String hash
    );
    void deleteMemberWith(Turn turn, User user);
    void deleteMemberWith(Long id);
    Optional<Member> getMemberWith(long id);
}
