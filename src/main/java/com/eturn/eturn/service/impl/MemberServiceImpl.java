package com.eturn.eturn.service.impl;

import com.eturn.eturn.entity.Member;
import com.eturn.eturn.enums.AccessEnum;
import com.eturn.eturn.repository.MemberRepository;
import com.eturn.eturn.service.MemberService;

public class MemberServiceImpl implements MemberService {
    MemberRepository memberRepository;
    @Override
    public void createMember(Long idUser, Long idTurn, AccessEnum access) {
        if (idUser==0 || idTurn==0){
            return;
        }
        Member member = new Member();
        member.setAccessEnum(access);
        member.setIdTurn(idTurn);
        member.setIdUser(idUser);
        memberRepository.save(member);
    }

    @Override
    public Member getMember(Long idUser, Long idTurn) {
        return memberRepository.getByIdUserAndIdTurn(idUser,idTurn);
    }

    @Override
    public AccessEnum getAccess(Long idUser, Long idTurn) {
        Member member = getMember(idUser, idTurn);
        return member.getAccessEnum();
    }

    @Override
    public void deleteTurnMembers(Long idTurn) {
        memberRepository.deleteByIdTurn(idTurn);
    }
}
