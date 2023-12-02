package com.eturn.eturn.service;

import com.eturn.eturn.entity.Member;
import com.eturn.eturn.enums.AccessEnum;
import org.springframework.stereotype.Service;

@Service
public interface MemberService {

    void createMember(Long idUser, Long idTurn, AccessEnum access);
    Member getMember(Long idUser, Long idTurn);

    AccessEnum getAccess(Long idUser, Long idTurn);
    void deleteTurnMembers(Long idTurn);
}
