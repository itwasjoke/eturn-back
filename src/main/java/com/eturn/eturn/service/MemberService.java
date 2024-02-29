package com.eturn.eturn.service;

import com.eturn.eturn.entity.Member;
import com.eturn.eturn.entity.Turn;
import com.eturn.eturn.entity.User;
import com.eturn.eturn.enums.AccessMemberEnum;

import java.util.List;


public interface MemberService {

    void createMember(Long userId, Long turnId, String access);
    Member getMember(Long idUser, Long idTurn);

    AccessMemberEnum getAccess(Long idUser, Long idTurn);
    void deleteTurnMembers(Long idTurn);

    List<Member> getListMemeberTurn(Long idTurn);

    long getConutByTurn(Long turnId);
}
