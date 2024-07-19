package com.eturn.eturn.service.impl;

import com.eturn.eturn.dto.MemberDTO;
import com.eturn.eturn.dto.mapper.MemberListMapper;
import com.eturn.eturn.entity.Member;
import com.eturn.eturn.entity.Turn;
import com.eturn.eturn.entity.User;
import com.eturn.eturn.enums.AccessMemberEnum;
import com.eturn.eturn.exception.member.NoAccessMemberException;
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
            Optional<Member> memberOptional = memberRepository.findMemberByUserAndTurn(user,turn);
            if (memberOptional.isPresent()){
                throw new UnknownMemberException("this member already exists");
            }
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
    public Member getMemberFrom(long id) {
        Optional<Member> member = memberRepository.findById(id);
        if (member.isPresent()){
            return member.get();
        }
        else{
            throw new NotFoundMemberException("member by id not found");
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
    public Boolean memberExist(User user, Turn turn) {
        Optional<Member> member = memberRepository.findMemberByUserAndTurn(user, turn);
        return member.isPresent();
    }

    @Override
    public long getCountMembers(Turn turn) {
        return memberRepository.countByTurn(turn);
    }


    @Override
    public List<MemberDTO> getMemberList(Turn turn, String type) {
        AccessMemberEnum accessMemberEnum = AccessMemberEnum.valueOf(type);
        List<Member> members = memberRepository.getMemberByTurnAndAccessMemberEnum(turn, accessMemberEnum);
        return memberListMapper.map(members);
    }

    @Override
    public void changeMemberStatus(long id, String type, User user) {
        Optional<Member> member = memberRepository.findById(id);
        if (member.isPresent()){
            Member memberGet = member.get();
            Optional<Member> memberUser = memberRepository.findMemberByUserAndTurn(user, memberGet.getTurn());
            if (memberUser.isPresent()){
                if (memberUser.get().getAccessMemberEnum()==AccessMemberEnum.MODERATOR ||
                        memberUser.get().getAccessMemberEnum()==AccessMemberEnum.CREATOR ){
                        if (memberGet.getAccessMemberEnum()!=AccessMemberEnum.CREATOR){
                            AccessMemberEnum accessMemberEnum = AccessMemberEnum.valueOf(type);
                            memberGet.setAccessMemberEnum(accessMemberEnum);
                            memberRepository.save(memberGet);
                        }
                }
                else{
                    throw new NoAccessMemberException("you don't have root for this operation");
                }
            }
            else{
                throw new NotFoundMemberException("can't change member status because your member not found");
            }

        }
        else{
            throw new NotFoundMemberException("no member what you want to update");
        }
    }

    @Override
    public void deleteMember(long id, User user) {
        Optional<Member> member = memberRepository.findById(id);
        if (member.isPresent()){
            Member memberGet = member.get();
            if (memberGet.getAccessMemberEnum()==AccessMemberEnum.CREATOR){
                throw new NoAccessMemberException("you cannot delete creator of the turn");
            }
            Optional<Member> memberUser = memberRepository.findMemberByUserAndTurn(user, memberGet.getTurn());
            if (memberUser.isPresent()){
                if (memberUser.get().getAccessMemberEnum()==AccessMemberEnum.MODERATOR ||
                        memberUser.get().getAccessMemberEnum()==AccessMemberEnum.CREATOR || memberGet.getUser()==user){
                    memberRepository.deleteById(id);
                }
                else{
                    throw new NoAccessMemberException("you don't have root for this operation");
                }
            }
            else{
                throw new NotFoundMemberException("can't delete because your member not found and this is not your member");
            }
        }
        else{
            throw new NotFoundMemberException("no member what you want to delete");
        }
    }

    @Override
    public void deleteMemberFrom(Turn turn, User user) {
        memberRepository.deleteByTurnAndUser(turn, user);
    }
}
