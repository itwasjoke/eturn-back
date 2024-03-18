package com.eturn.eturn.service;

import com.eturn.eturn.entity.Member;
import com.eturn.eturn.entity.Turn;
import com.eturn.eturn.entity.User;
import com.eturn.eturn.enums.AccessMemberEnum;

import java.util.List;


public interface MemberService {

    void createMember(Long userId, Long turnId, String access);
    Member getMember(String username, Long idTurn);

    AccessMemberEnum getAccess(Long userId, Long idTurn);

    long getCountMembers(Long turnId);
    void deleteTurnMembers(Long idTurn);

}
