package com.eturn.eturn.service.impl;

import com.eturn.eturn.dto.MemberDTO;
import com.eturn.eturn.dto.MemberListDTO;
import com.eturn.eturn.dto.mapper.MemberListMapper;
import com.eturn.eturn.entity.*;
import com.eturn.eturn.enums.AccessMember;
import com.eturn.eturn.enums.AccessTurn;
import com.eturn.eturn.enums.InvitedStatus;
import com.eturn.eturn.exception.member.NoAccessMemberException;
import com.eturn.eturn.exception.member.NotFoundMemberException;
import com.eturn.eturn.exception.member.UnknownMemberException;
import com.eturn.eturn.exception.position.NoInviteException;
import com.eturn.eturn.notifications.NotificationController;
import com.eturn.eturn.repository.MemberRepository;
import com.eturn.eturn.service.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;


@Service
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;
    private final MemberListMapper memberListMapper;
    private final UserService userService;
    private final PositionService positionService;
    private final TurnService turnService;
    private final NotificationController notificationController;
    public MemberServiceImpl(MemberRepository memberRepository, MemberListMapper memberListMapper, UserService userService, PositionService positionService, TurnService turnService, NotificationController notificationController) {
        this.memberRepository = memberRepository;
        this.memberListMapper = memberListMapper;
        this.userService = userService;
        this.positionService = positionService;
        this.turnService = turnService;
        this.notificationController = notificationController;
    }

    @Transactional
    @Override
    public Member createMember(User user, Turn turn, String access, boolean invitedForTurn) {
        try {
            Optional<Member> memberOptional = memberRepository.findMemberByUserAndTurn(user,turn);
            if (memberOptional.isPresent()){
                throw new UnknownMemberException("this member already exists");
            }
            InvitedStatus status;
            AccessMember accessMember = AccessMember.valueOf(access);
            if (invitedForTurn) {
                status = InvitedStatus.INVITED;
            }
            else if (accessMember == AccessMember.MEMBER_LINK) {
                if (turn.getAccessTurnType() == AccessTurn.FOR_ALLOWED_ELEMENTS) {
                    Set<Group> groups = turn.getAllowedGroups();
                    Set<Faculty> faculties = turn.getAllowedFaculties();
                    if (groups.contains(user.getGroup()) || faculties.contains(user.getGroup().getFaculty())) {
                        status = InvitedStatus.ACCESS_IN;
                    } else {
                        status = InvitedStatus.ACCESS_OUT;
                    }
                }
                else {
                    status = InvitedStatus.ACCESS_OUT;
                }
            } else {
                status = InvitedStatus.ACCESS_IN;
            }
            Member member = new Member();
            member.setAccessMember(accessMember);
            member.setTurn(turn);
            member.setUser(user);
            member.setInvitedForTurn(status);
            return memberRepository.save(member);
        }
        catch(Exception e){
            throw new UnknownMemberException("Cannot create member on createMember method MemberServiceImpl.java");
        }
    }

    @Override
    public Optional<Member> getMemberWith(long id) {
        return memberRepository.findById(id);
    }

    @Override
    public Optional<Member> getMemberWith(User user, Turn turn) {
        return memberRepository.findMemberByUserAndTurn(user, turn);
    }

    @Override
    public AccessMember getAccess(User user, Turn turn) {
        Optional<Member> m = memberRepository.findMemberByUserAndTurn(user, turn);
        return m.map(Member::getAccessMember).orElse(null);
    }

    @Override
    public MemberDTO getMemberDTO(User user, Turn turn) {
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
                    memberToDTO.getInvitedForTurn().toString()
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
    public int countInviteModerators(Turn turn) {
        return memberRepository.countByTurnAndInvited(turn, true);
    }

    @Override
    public int countInviteMembers(Turn turn) {
        return memberRepository.countByTurnAndInvitedForTurn(turn, InvitedStatus.INVITED);
    }

    @Override
    public long countBlocked(Turn turn) {
        return memberRepository.countByTurnAndAccessMember(turn, AccessMember.BLOCKED);
    }

    @Override
    @Transactional
    public void changeMemberStatus(long id, String type, String username) {
        User user = userService.getUserFromLogin(username);
        if (!Objects.equals(type, "MEMBER") && !Objects.equals(type, "BLOCKED")){
            throw new NoAccessMemberException("You cant change status on MODERATOR");
        }
        Member member = changeMemberStatus(id, type, user);
        boolean positionExist = positionService.existsAllByTurnAndUser(member.getTurn(), member.getUser());
        if (member.getAccessMember() == AccessMember.MEMBER){
            Turn turn = member.getTurn();
            if (
                    !positionExist
                            && turn.getAccessTurnType() == AccessTurn.FOR_ALLOWED_ELEMENTS
            ){
                deleteMemberWith(member.getId());
            } else if (
                    !positionExist
                            && turn.getAccessTurnType() == AccessTurn.FOR_LINK
            ) {
                member = changeMemberStatus(id, "MEMBER_LINK", user);
            }
        }
        if (type.equals("BLOCKED")) {
            positionService.deleteAllByTurnAndUser(member.getTurn(), member.getUser());
        }
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
                        if (memberGet.getAccessMember() != AccessMember.CREATOR){
                            if (!Objects.equals(type, "MEMBER") && !Objects.equals(type, "MODERATOR") && !Objects.equals(type, "BLOCKED") && !Objects.equals(type, "MEMBER_LINK")){
                                throw new NoAccessMemberException("no access");
                            }
                            if (memberUser.get().getAccessMember()!= AccessMember.CREATOR && type.equals("MODERATOR")){
                                throw new NoAccessMemberException("no access");
                            }
                            AccessMember accessMember = AccessMember.valueOf(type);
                            if (accessMember == AccessMember.BLOCKED) {
                                memberGet.setInvitedForTurn(InvitedStatus.ACCESS_OUT);
                                memberGet.setInvited(false);
                            }
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
                if (invitedTurn == 0) memberGet.setInvitedForTurn(InvitedStatus.ACCESS_IN);
                if (invitedTurn == 1) memberGet.setInvitedForTurn(InvitedStatus.INVITED);
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
    public void deleteMemberWith(Turn turn, User user) {
        memberRepository.deleteByTurnAndUser(turn, user);
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
    public void deleteMemberWith(Long id) {
        memberRepository.deleteById(id);
    }
    @Override
    public void changeMemberInvite(Long id, boolean status, boolean isModerator) {
        Optional<Member> memberPresent = getMemberWith(id);
        if (memberPresent.isPresent()) {
            Member member = memberPresent.get();
            boolean isInvited = member.isInvited();
            InvitedStatus invitedStatus = member.getInvitedForTurn();
            if (status) {
                if (isInvited && isModerator) {
                    changeMemberStatusFrom(id, "MODERATOR", 0, 0);
                }
                if (invitedStatus == InvitedStatus.INVITED) {
                    if (!isModerator) {
                        if (isInvited) {
                            changeMemberStatusFrom(id, "MEMBER", 1, 0);
                        } else {
                            changeMemberStatusFrom(id, "MEMBER", 0, 0);
                        }
                    } else if (!isInvited){
                        throw new NoInviteException("User not invite to moderator");
                    }
                    positionService.createPositionAndSave(member.getUser().getLogin(), member.getTurn().getHash());
                }
            } else {
                if (isInvited && isModerator) {
                    changeMemberInvite(id, false);
                }
                if (invitedStatus == InvitedStatus.INVITED && !isModerator) {
                    if (isInvited) {
                        changeMemberStatusFrom(id, "MEMBER_LINK", 1, 0);
                    } else {
                        deleteMemberWith(id);
                    }
                }
            }

        } else {
            throw new NotFoundMemberException("Member not found");
        }
    }
    @Override
    public boolean invitedExists(Turn turn) {
        return memberRepository.getOneInvitedExists(turn, true, InvitedStatus.INVITED).isPresent();
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
    @Override
    @Transactional
    public void inviteMember(String hash, String username) {
        User user = userService.getUserFromLogin(username);
        Turn turn = turnService.getTurnFrom(hash);
        if (countInviteModerators(turn) > 20 || getCountModerators(turn) > 20) {
            throw new NoInviteException("You cant invite");
        }
        if (turn.getCreator() == user) {
            throw new NoAccessMemberException("You are creator");
        }
        Optional<Member> memberPresent = getMemberWith(user, turn);
        if (memberPresent.isPresent()) {
            if (memberPresent.get().getAccessMember() == AccessMember.BLOCKED) {
                throw new NoAccessMemberException("You are blocked");
            }
            changeMemberInvite(memberPresent.get().getId(), true);
            notificationController.notifyReceiptRequest(turn.getId(), turn.getName());
        } else {
            Member member = createMember(user, turn, "MEMBER_LINK", false);
            changeMemberInvite(member.getId(), true);
            notificationController.notifyReceiptRequest(turn.getId(), turn.getName());
        }
    }
    @Override
    public MemberListDTO getMemberList(String username, String type, String hash, int page) {
        User user = userService.getUserFromLogin(username);
        Turn turn = turnService.getTurnFrom(hash);
        Optional<Member> member = getMemberWith(user, turn);
        if (member.isPresent()){
            AccessMember access = member.get().getAccessMember();
            if (access == AccessMember.CREATOR || access == AccessMember.MODERATOR){
                Pageable paging = PageRequest.of(page, 20);
                AccessMember accessMember = AccessMember.valueOf(type);
                Page<Member> members = memberRepository.getMemberByTurnAndAccessMember(turn, accessMember, paging);
                long count = memberRepository.countByTurnAndAccessMember(turn, accessMember);
                return new MemberListDTO(memberListMapper.map(members), count);
            }
            else{
                throw new NoAccessMemberException("No access");
            }
        }
        else {
            throw new NotFoundMemberException("no member");
        }
    }

    @Override
    public MemberListDTO getUnconfirmedMemberList(String username, String type, String hash) {
        User user = userService.getUserFromLogin(username);
        Turn turn = turnService.getTurnFrom(hash);
        Optional<Member> member = getMemberWith(user, turn);
        if (member.isPresent()){
            AccessMember access = member.get().getAccessMember();
            if (access == AccessMember.CREATOR || access == AccessMember.MODERATOR){
                AccessMember accessMember = AccessMember.valueOf(type);
                List<Member> members = null;
                long count = 0L;
                if (accessMember == AccessMember.MODERATOR) {
                    members = memberRepository.getMemberByTurnAndInvited(turn, true);
                    count = memberRepository.countByTurnAndInvited(turn, true);
                } else if (accessMember == AccessMember.MEMBER) {
                    Pageable paging = PageRequest.of(0, 20);
                    Page<Member> page = memberRepository.getMemberByTurnAndAccessMemberAndInvitedForTurn(turn, AccessMember.MEMBER_LINK, InvitedStatus.INVITED, paging);
                    count = memberRepository.countByTurnAndInvitedForTurn(turn, InvitedStatus.INVITED);
                    members = page.toList();
                }
                return new MemberListDTO(memberListMapper.mapMember(members), count);
            }
            else{
                throw new NoAccessMemberException("No access");
            }
        }
        else {
            throw new NotFoundMemberException("no member");
        }
    }
}
