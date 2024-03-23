package com.eturn.eturn.service.impl;

import com.eturn.eturn.dto.MemberDTO;
import com.eturn.eturn.dto.mapper.MemberListMapper;
import com.eturn.eturn.entity.Member;
import com.eturn.eturn.entity.Turn;
import com.eturn.eturn.entity.User;
import com.eturn.eturn.enums.AccessMemberEnum;
import com.eturn.eturn.exception.member.NotFoundMemberException;
import com.eturn.eturn.exception.member.UnknownMemberException;
import com.eturn.eturn.repository.MemberRepository;
import com.eturn.eturn.service.MemberService;
import com.eturn.eturn.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Service
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;
    private final UserService userService;
//    private final TurnService turnService;
    private final MemberListMapper memberListMapper;
    public MemberServiceImpl(MemberRepository memberRepository, UserService userService, MemberListMapper memberListMapper) {
        this.memberRepository = memberRepository;
        this.userService = userService;
        this.memberListMapper = memberListMapper;
    }

    @Transactional
    @Override
    public void createMember(User user, Turn turn, String access) {
        try {
            AccessMemberEnum accessMemberEnum = AccessMemberEnum.valueOf(access);
            Member member = new Member();
            member.setAccessMemberEnum(accessMemberEnum);
            member.setTurn(turn);
            member.setUser(user);
            memberRepository.save(member);
        }
        catch(Exception e){
            throw new UnknownMemberException("Cannot create member on createMember method MemberServiceImpl.java");
        }
    }

    @Override
    public AccessMemberEnum getAccess(User user, Turn turn) {
        return memberRepository.getByUserAndTurn(user, turn).getAccessMemberEnum();
    }

    @Override
    public MemberDTO getMember(User user, Turn turn) {
        Optional<Member> member = memberRepository.findMemberByUserAndTurn(user, turn);
        if (member.isPresent()){
            Member memberToDTO = member.get();
            return new MemberDTO(
                    memberToDTO.getId(),
                    memberToDTO.getUser().getId(),
                    memberToDTO.getTurn().getId(),
                    memberToDTO.getUser().getName(),
                    memberToDTO.getAccessMemberEnum().toString()
                    );
        }
        else{
            throw new NotFoundMemberException("Cannot find member on getMember method MemberServiceImpl.java");
        }
    }

    @Override
    public long getCountMembers(Turn turn) {
        return memberRepository.countByTurn(turn);
    }

    @Override
    public void deleteTurnMembers(Turn turn) {
        try{
            memberRepository.deleteByTurn(turn);
        } catch(RuntimeException e){
            throw new UnknownMemberException("Cannot delete member on createMember method MemberServiceImpl.java " + e.getMessage());
        }
    }

    @Override
    public List<MemberDTO> getMemberList(Turn turn, String type) {
        AccessMemberEnum accessMemberEnum = AccessMemberEnum.valueOf(type);
        List<Member> members = memberRepository.getMemberByTurnAndAccessMemberEnum(turn, accessMemberEnum);
        return memberListMapper.map(members);
    }
}
