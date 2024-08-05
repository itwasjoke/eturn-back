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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public Member createMember(User user, Turn turn, String access, boolean invitedForTurn) {
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
                    memberToDTO.getUser().getGroup().getNumber(),
                    memberToDTO.getAccessMemberEnum().toString(),
                    memberToDTO.isInvited(),
                    memberToDTO.isInvitedForTurn()
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
        return memberRepository.countByTurnAndAccessMemberEnum(turn, AccessMemberEnum.MEMBER);
    }

    @Override
    public long getCountModerators(Turn turn) {
        return memberRepository.countByTurnAndAccessMemberEnum(turn, AccessMemberEnum.MODERATOR);
    }

    @Override
    public List<MemberDTO> getMemberList(Turn turn, String type, Pageable pageable) {
        AccessMemberEnum accessMemberEnum = AccessMemberEnum.valueOf(type);
        Page<Member> members = memberRepository.getMemberByTurnAndAccessMemberEnum(turn, accessMemberEnum, pageable);
        return memberListMapper.map(members);
    }

    @Override
    public List<MemberDTO> getUnconfMemberList(Turn turn, String type) {
        AccessMemberEnum accessMemberEnum = AccessMemberEnum.valueOf(type);
        List<Member> members = null;
        if (accessMemberEnum == AccessMemberEnum.MODERATOR) {
            members = memberRepository.getMemberByTurnAndInvited(turn, true);
        } else if (accessMemberEnum == AccessMemberEnum.MEMBER) {
            members = memberRepository.getMemberByTurnAndAccessMemberEnumAndInvitedForTurn(turn, AccessMemberEnum.MEMBER_LINK, true);
        }
        return memberListMapper.mapMember(members);
    }

    @Override
    public int countInviteModerators(Turn turn) {
        return memberRepository.countByTurnAndInvited(turn, true);
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
                            if (!Objects.equals(type, "MEMBER") && !Objects.equals(type, "MODERATOR") && !Objects.equals(type, "BLOCKED") && !Objects.equals(type, "MEMBER_LINK")){
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
    public void changeMemberStatusFrom(long id, String type, int invitedModerator, int invitedTurn) {
        Optional<Member> member = memberRepository.findById(id);
        if (member.isPresent()) {
            Member memberGet = member.get();
            if (invitedModerator != -1) {
                if (invitedModerator == 0) memberGet.setInvited(false);
                if (invitedModerator == 1) memberGet.setInvited(true);
            }
            if (invitedTurn != -1) {
                if (invitedModerator == 0) memberGet.setInvitedForTurn(false);
                if (invitedModerator == 1) memberGet.setInvitedForTurn(true);
            }
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
    public boolean invitedExists(Turn turn) {
        return memberRepository.existsAllByTurnAndInvitedOrInvitedForTurn(turn, true, true);
    }

    @Override
    public void changeMemberInviteForTurn(Long id, boolean status) {
        Optional<Member> memberPresent = memberRepository.findById(id);
        if (memberPresent.isPresent()) {
            Member member = memberPresent.get();
            member.setInvitedForTurn(status);
            memberRepository.save(member);
        } else {
            throw new NotFoundMemberException("Not found member");
        }
    }
}
