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
import java.util.Objects;
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
    public Member createMember(User user, Turn turn, String access) {
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
            return memberRepository.save(member);
        }
        catch(Exception e){
            throw new UnknownMemberException("Cannot create member on createMember method MemberServiceImpl.java");
        }
    }

    @Override
    public Optional<Member> getMemberFrom(long id) {
        return memberRepository.findById(id);
    }

    @Override
    public Optional<Member> getOptionalMember(User user, Turn turn) {
        return memberRepository.findMemberByUserAndTurn(user, turn);
    }

    @Override
    public AccessMemberEnum getAccess(User user, Turn turn) {
        Optional<Member> m = memberRepository.findMemberByUserAndTurn(user, turn);
        if (m.isPresent()){
            return m.get().getAccessMemberEnum();
        } else {
            return null;
        }
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
    public Member changeMemberStatus(long id, String type, User user) {
        Optional<Member> member = memberRepository.findById(id);
        if (member.isPresent()){
            Member memberGet = member.get();
            Optional<Member> memberUser = memberRepository.findMemberByUserAndTurn(user, memberGet.getTurn());
            if (memberUser.isPresent()){
                if (memberUser.get().getAccessMemberEnum()==AccessMemberEnum.MODERATOR ||
                        memberUser.get().getAccessMemberEnum()==AccessMemberEnum.CREATOR ){
                        if (memberGet.getAccessMemberEnum()!=AccessMemberEnum.CREATOR){
                            if (!Objects.equals(type, "MEMBER") && !Objects.equals(type, "MODERATOR") && !Objects.equals(type, "BLOCKED")){
                                throw new NoAccessMemberException("no access");
                            }
                            if (memberUser.get().getAccessMemberEnum()!=AccessMemberEnum.CREATOR && type.equals("MODERATOR")){
                                throw new NoAccessMemberException("no access");
                            }
                            AccessMemberEnum accessMemberEnum = AccessMemberEnum.valueOf(type);
                            memberGet.setAccessMemberEnum(accessMemberEnum);
                            return memberRepository.save(memberGet);
                        }
                        else {
                            throw new NoAccessMemberException("you are creator");
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
    public void changeMemberStatusFrom(long id, String type) {
        Optional<Member> member = memberRepository.findById(id);
        if (member.isPresent()) {
            Member memberGet = member.get();
            AccessMemberEnum accessMemberEnum = AccessMemberEnum.valueOf(type);
            memberGet.setAccessMemberEnum(accessMemberEnum);
            memberRepository.save(memberGet);
        }
        else{
            throw new NotFoundMemberException("no member what you want to update");
        }
    }

    @Override
    public Member deleteMember(long id, User user) {
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
                    Member memberDeleted = member.get();
                    memberRepository.deleteById(id);
                    return memberDeleted;
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

    @Override
    public void deleteMemberFrom(Long id) {
        memberRepository.deleteById(id);
    }

    @Override
    public void deleteMembersWithoutPositions(Turn turn) {
        memberRepository.deleteMembersWithoutPositions(turn);
    }

    @Override
    public void changeMemberInvite(Long id, boolean status) {
        Optional<Member> memberPresent = memberRepository.findById(id);
        if (memberPresent.isPresent()) {
            Member member = memberPresent.get();
            member.setInvited(status);
            memberRepository.save(member);
        } else {
            throw new NotFoundMemberException("Not found member");
        }
    }

    @Override
    public void changeMemberStatus(Long id, boolean status) {
        Optional<Member> memberPresent = memberRepository.findById(id);
        if (memberPresent.isPresent()) {
            Member member = memberPresent.get();
            member.setInvitedMember(status);
            memberRepository.save(member);
        } else {
            throw new NotFoundMemberException("Not found member");
        }
    }
}
