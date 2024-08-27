package com.eturn.eturn.service.impl;

import com.eturn.eturn.dto.MemberDTO;
import com.eturn.eturn.dto.mapper.MemberListMapper;
import com.eturn.eturn.entity.Member;
import com.eturn.eturn.entity.Turn;
import com.eturn.eturn.entity.User;
import com.eturn.eturn.enums.AccessMember;
import com.eturn.eturn.exception.member.NoAccessMemberException;
import com.eturn.eturn.exception.member.NotFoundMemberException;
import com.eturn.eturn.exception.member.UnknownMemberException;
import com.eturn.eturn.repository.MemberRepository;
import com.eturn.eturn.service.MemberService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


@Service
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;
    private final MemberListMapper memberListMapper;
    public MemberServiceImpl(MemberRepository memberRepository, MemberListMapper memberListMapper) {
        this.memberRepository = memberRepository;
        this.memberListMapper = memberListMapper;
    }

    @Transactional
    @Override
    public Member createMember(User user, Turn turn, String access, boolean invitedForTurn) {
        try {
            Optional<Member> memberOptional = memberRepository.findMemberByUserAndTurn(user,turn);
            if (memberOptional.isPresent()){
                throw new UnknownMemberException("this member already exists");
            }
            AccessMember accessMember = AccessMember.valueOf(access);
            Member member = new Member();
            member.setAccessMember(accessMember);
            member.setTurn(turn);
            member.setUser(user);
            member.setInvitedForTurn(invitedForTurn);
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
    public AccessMember getAccess(User user, Turn turn) {
        Optional<Member> m = memberRepository.findMemberByUserAndTurn(user, turn);
        return m.map(Member::getAccessMember).orElse(null);
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
                    memberToDTO.getUser().getGroup().getNumber(),
                    memberToDTO.getAccessMember().toString(),
                    memberToDTO.isInvited(),
                    memberToDTO.isInvitedForTurn()
            );
        }
        else{
            throw new NotFoundMemberException("Cannot find member on getMember method MemberServiceImpl.java");
        }
    }

    @Override
    public long getCountMembers(Turn turn) {
        return memberRepository.countByTurnAndAccessMember(turn, AccessMember.MEMBER);
    }

    @Override
    public long getCountModerators(Turn turn) {
        return memberRepository.countByTurnAndAccessMember(turn, AccessMember.MODERATOR);
    }

    @Override
    public List<MemberDTO> getMemberList(Turn turn, String type, Pageable pageable) {
        AccessMember accessMember = AccessMember.valueOf(type);
        Page<Member> members = memberRepository.getMemberByTurnAndAccessMember(turn, accessMember, pageable);
        return memberListMapper.map(members);
    }

    @Override
    public List<MemberDTO> getUnconfMemberList(Turn turn, String type) {
        AccessMember accessMember = AccessMember.valueOf(type);
        List<Member> members = null;
        if (accessMember == AccessMember.MODERATOR) {
            members = memberRepository.getMemberByTurnAndInvited(turn, true);
        } else if (accessMember == AccessMember.MEMBER) {
            members = memberRepository.getMemberByTurnAndAccessMemberAndInvitedForTurn(turn, AccessMember.MEMBER_LINK, true);
        }
        return memberListMapper.mapMember(members);
    }

    @Override
    public int countInviteModerators(Turn turn) {
        return memberRepository.countByTurnAndInvited(turn, true);
    }

    @Override
    public int countInviteMembers(Turn turn) {
        return memberRepository.countByTurnAndInvitedForTurn(turn, true);
    }

    @Override
    public long countBlocked(Turn turn) {
        return memberRepository.countByTurnAndAccessMember(turn, AccessMember.BLOCKED);
    }

    @Override
    public Member changeMemberStatus(long id, String type, User user) {
        Optional<Member> member = memberRepository.findById(id);
        if (member.isPresent()){
            Member memberGet = member.get();
            Optional<Member> memberUser = memberRepository.findMemberByUserAndTurn(user, memberGet.getTurn());
            if (memberUser.isPresent()){
                if (memberUser.get().getAccessMember()== AccessMember.MODERATOR ||
                        memberUser.get().getAccessMember()== AccessMember.CREATOR ){
                        if (memberGet.getAccessMember()!= AccessMember.CREATOR){
                            if (!Objects.equals(type, "MEMBER") && !Objects.equals(type, "MODERATOR") && !Objects.equals(type, "BLOCKED") && !Objects.equals(type, "MEMBER_LINK")){
                                throw new NoAccessMemberException("no access");
                            }
                            if (memberUser.get().getAccessMember()!= AccessMember.CREATOR && type.equals("MODERATOR")){
                                throw new NoAccessMemberException("no access");
                            }
                            AccessMember accessMember = AccessMember.valueOf(type);
                            memberGet.setAccessMember(accessMember);
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
    public void changeMemberStatusFrom(long id, String type, int invitedModerator, int invitedTurn) {
        Optional<Member> member = memberRepository.findById(id);
        if (member.isPresent()) {
            Member memberGet = member.get();
            if (invitedModerator != -1) {
                if (invitedModerator == 0) memberGet.setInvited(false);
                if (invitedModerator == 1) memberGet.setInvited(true);
            }
            if (invitedTurn != -1) {
                if (invitedTurn == 0) memberGet.setInvitedForTurn(false);
                if (invitedTurn == 1) memberGet.setInvitedForTurn(true);
            }
            AccessMember accessMember = AccessMember.valueOf(type);
            memberGet.setAccessMember(accessMember);
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
            if (memberGet.getAccessMember()== AccessMember.CREATOR){
                throw new NoAccessMemberException("you cannot delete creator of the turn");
            }
            Optional<Member> memberUser = memberRepository.findMemberByUserAndTurn(user, memberGet.getTurn());
            if (memberUser.isPresent()){
                if (memberUser.get().getAccessMember()== AccessMember.MODERATOR ||
                        memberUser.get().getAccessMember()== AccessMember.CREATOR || memberGet.getUser()==user){
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
    public boolean invitedExists(Turn turn) {
        return memberRepository.getOneInvitedExists(turn, true, true).isPresent();
    }

    @Override
    public List<User> getModeratorsOfTurn(long turnId) {
        List<Member> members = memberRepository.getAllByTurn_IdAndAccessMember(turnId, AccessMember.MODERATOR);
        Member creator = memberRepository.getMemberByTurn_IdAndAccessMember(turnId, AccessMember.CREATOR);
        List<User> users = new ArrayList<>();
        for (Member m: members) {
            users.add(m.getUser());
        }
        users.add(creator.getUser());
        return users;
    }
}
