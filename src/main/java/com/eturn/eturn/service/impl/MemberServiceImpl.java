package com.eturn.eturn.service.impl;

import com.eturn.eturn.entity.Member;
import com.eturn.eturn.enums.AccessMemberEnum;
import com.eturn.eturn.repository.MemberRepository;
import com.eturn.eturn.service.MemberService;
import org.springframework.stereotype.Service;

@Service
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;

    public MemberServiceImpl(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public void createMember(Long idUser, Long idTurn, AccessMemberEnum access) {
        if (idUser==0 || idTurn==0){
            return;
        }
        Member member = new Member();
        member.setAccessMemberEnum(access);
        member.setIdTurn(idTurn);
        member.setIdUser(idUser);
        memberRepository.save(member);
    }

    @Override
    public Member getMember(Long idUser, Long idTurn) {
        return memberRepository.getByIdUserAndIdTurn(idUser,idTurn);
    }

    @Override
    public AccessMemberEnum getAccess(Long idUser, Long idTurn) {
        Member member = getMember(idUser, idTurn);
        return member.getAccessMemberEnum();
    }

    @Override
    public void deleteTurnMembers(Long idTurn) {
        memberRepository.deleteByIdTurn(idTurn);
    }
}
