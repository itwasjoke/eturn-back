package com.eturn.eturn.service;

import com.eturn.eturn.dto.MemberDTO;
import com.eturn.eturn.entity.Turn;
import com.eturn.eturn.entity.User;
import com.eturn.eturn.enums.AccessMemberEnum;

import java.util.List;


public interface MemberService {

    void createMember(User user, Turn turn, String access);


    AccessMemberEnum getAccess(User user, Turn turn);

    MemberDTO getMember(User user, Turn turn);

    long getCountMembers(Turn turn);
    void deleteTurnMembers(Turn turn);

    List<MemberDTO> getMemberList(Turn turn, String type);

    void changeMemberStatus(long id, String type, User user);

    void deleteMember(long id, User user);

}
