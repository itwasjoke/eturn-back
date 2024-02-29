package com.eturn.eturn.service.impl;

import com.eturn.eturn.entity.Member;
import com.eturn.eturn.enums.AccessMemberEnum;
import com.eturn.eturn.exception.member.UnknownMemberException;
import com.eturn.eturn.exception.user.NotFoundUserException;
import com.eturn.eturn.repository.MemberRepository;
import com.eturn.eturn.service.MemberService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
    public void createMember(Long userId, Long turnId, String access) {
        try {
            AccessMemberEnum accessMemberEnum = AccessMemberEnum.valueOf(access);
            Member member = new Member();
            member.setAccessMemberEnum(accessMemberEnum);
            member.setIdTurn(turnId);
            member.setIdUser(userId);
            memberRepository.save(member);
        }
        catch(Exception e){
            throw new UnknownMemberException("Cannot create member on createMember method MemberServiceImpl.java");
        }
    }

    @Override
    public Member getMember(Long idUser, Long idTurn) {
        try{
            return memberRepository.getByIdUserAndIdTurn(idUser,idTurn);
        } catch (RuntimeException e){
            throw new NotFoundUserException("Member not found in getMember method (MemberServiceImpl.java)");
        }

    }

    @Override
    public AccessMemberEnum getAccess(Long idUser, Long idTurn) {
        Member member = getMember(idUser, idTurn);
        return member.getAccessMemberEnum();
    }

    @Override
    public void deleteTurnMembers(Long idTurn) {
        try{
            memberRepository.deleteByIdTurn(idTurn);
        } catch(RuntimeException e){
            throw new UnknownMemberException("Cannot delete member on createMember method MemberServiceImpl.java " + e.getMessage());
        }

    }

    @Override
    public List<Member> getListMemeberTurn(Long idTurn){
        try {
            return memberRepository.getMemberByIdTurn(idTurn);
        } catch(RuntimeException e){
            throw new UnknownMemberException("Cannot get member on createMember method MemberServiceImpl.java " + e.getMessage());
        }
    }

    @Override
    public long getConutByTurn(Long turnId) {
        try {
            return memberRepository.countByIdTurn(turnId);
        } catch(RuntimeException e){
            throw new UnknownMemberException("Cannot get count member on createMember method MemberServiceImpl.java " + e.getMessage());
        }
    }
}
