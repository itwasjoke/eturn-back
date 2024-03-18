package com.eturn.eturn.service.impl;

import com.eturn.eturn.dto.UserDTO;
import com.eturn.eturn.entity.Member;
import com.eturn.eturn.enums.AccessMemberEnum;
import com.eturn.eturn.exception.member.NotFoundMemberException;
import com.eturn.eturn.exception.member.UnknownMemberException;
import com.eturn.eturn.exception.user.NotFoundUserException;
import com.eturn.eturn.repository.MemberRepository;
import com.eturn.eturn.service.MemberService;
import com.eturn.eturn.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Service
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;
    private final UserService userService;
//    private final TurnService turnService;
    public MemberServiceImpl(MemberRepository memberRepository, UserService userService) {
        this.memberRepository = memberRepository;
        this.userService = userService;
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
    public Member getMember(String username, Long idTurn) {

        UserDTO user = userService.getUser(username);
        Optional<Member> member = Optional.ofNullable(memberRepository.findByIdUserAndIdTurn(user.id(), idTurn));
        if (member.isPresent()){
            return member.get();
        }
        else{
            throw new NotFoundMemberException("no member");
        }
    }

    @Override
    public AccessMemberEnum getAccess(Long userId, Long idTurn) {
        return memberRepository.getByIdUserAndIdTurn(userId, idTurn).getAccessMemberEnum();
    }

    @Override
    public long getCountMembers(Long turnId) {
        return memberRepository.countByIdTurn(turnId);
    }

    @Override
    public void deleteTurnMembers(Long idTurn) {
        try{
            memberRepository.deleteByIdTurn(idTurn);
        } catch(RuntimeException e){
            throw new UnknownMemberException("Cannot delete member on createMember method MemberServiceImpl.java " + e.getMessage());
        }

    }
}
