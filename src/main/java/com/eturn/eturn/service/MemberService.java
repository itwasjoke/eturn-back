package com.eturn.eturn.service;

import com.eturn.eturn.entity.Member;
import com.eturn.eturn.enums.AccessMemberEnum;


public interface MemberService {

    void createMember(Long idUser, Long idTurn, AccessMemberEnum access);
    Member getMember(Long idUser, Long idTurn);

    AccessMemberEnum getAccess(Long idUser, Long idTurn);
    void deleteTurnMembers(Long idTurn);
}
