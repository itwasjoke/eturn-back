package com.eturn.eturn.service;

import com.eturn.eturn.dto.MemberDTO;
import com.eturn.eturn.entity.Member;
import com.eturn.eturn.entity.Turn;
import com.eturn.eturn.entity.User;
import com.eturn.eturn.enums.AccessMemberEnum;

import java.util.List;


public interface MemberService {

    void createMember(User user, Turn turn, String access);

    Member getMemberFrom(long id);
    AccessMemberEnum getAccess(User user, Turn turn);

    MemberDTO getMember(User user, Turn turn);

    long getCountMembers(Turn turn);

    List<MemberDTO> getMemberList(Turn turn, String type);

    void changeMemberStatus(long id, String type, User user);

    void deleteMember(long id, User user);

    void deleteMemberFrom(Turn turn, User user);

}
