package com.eturn.eturn.service;

import com.eturn.eturn.dto.MemberDTO;
import com.eturn.eturn.entity.Member;
import com.eturn.eturn.entity.Turn;
import com.eturn.eturn.entity.User;
import com.eturn.eturn.enums.AccessMemberEnum;

import java.util.List;
import java.util.Optional;


public interface MemberService {

    Member createMember(User user, Turn turn, String access);

    Optional<Member> getMemberFrom(long id);

    Optional<Member> getOptionalMember(User user, Turn turn);
    AccessMemberEnum getAccess(User user, Turn turn);

    MemberDTO getMember(User user, Turn turn);

    Boolean memberExist(User user, Turn turn);

    long getCountMembers(Turn turn);

    List<MemberDTO> getMemberList(Turn turn, String type);

    Member changeMemberStatus(long id, String type, User user);

    void changeMemberStatusFrom(long id, String type);

    Member deleteMember(long id, User user);

    void deleteMemberFrom(Turn turn, User user);

    void deleteMemberFrom(Long id);
    void deleteMembersWithoutPositions(Turn turn);
    void changeMemberInvite(Long id, boolean status);

}
