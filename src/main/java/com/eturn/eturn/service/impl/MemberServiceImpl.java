package com.eturn.eturn.service.impl;

import com.eturn.eturn.entity.Member;
import com.eturn.eturn.enums.AccessMemberEnum;
import com.eturn.eturn.repository.MemberRepository;
import com.eturn.eturn.service.MemberService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;
//    private final TurnService turnService;
    public MemberServiceImpl(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Transactional
    @Override
    public void createMember(Long userId, Long turnId, AccessMemberEnum access) {
        Member member = new Member();
        member.setAccessMemberEnum(access);
        member.setIdTurn(turnId);
        member.setIdUser(userId);
//        turnService.addTurnToUser(turnId, userId);
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

    @Override
    public List<Member> getListMemeberTurn(Long idTurn){
        return memberRepository.getMemberByIdTurn(idTurn);
    }
}
