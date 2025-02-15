package com.eturn.eturn.service;

import com.eturn.eturn.dto.MemberDTO;
import com.eturn.eturn.dto.MemberListDTO;
import com.eturn.eturn.entity.Member;
import com.eturn.eturn.entity.Turn;
import com.eturn.eturn.entity.User;
import com.eturn.eturn.enums.AccessMember;
import com.eturn.eturn.enums.ChangeMbrAction;
import com.eturn.eturn.enums.MemberListType;

import java.util.List;
import java.util.Optional;


public interface MemberService {
    void setInviteForMember(String hash, String username);
    Member createMember(User user, Turn turn, String access, boolean invitedForTurn);
    Optional<Member> getMemberWith(long id);
    Optional<Member> getMemberWith(User user, Turn turn);
    MemberDTO getMemberDTO(User user, Turn turn);
    AccessMember getAccess(User user, Turn turn);
    int getCountMembersWith(Turn turn, MemberListType memberListType);
    MemberListDTO getMemberList(String username, String type, String hash, int page);
    MemberListDTO getUnconfirmedMemberList(String username, String type, String hash);
    void setBlockStatus(long id, String type, String username);
    void changeMemberStatusFrom(long id, String type, Optional<ChangeMbrAction> actionMod, Optional<ChangeMbrAction> actionTurn);
    void changeMemberInvite(Long id, boolean status, boolean isModerator);
    void deleteMemberWith(Turn turn, User user);
    void deleteMembersWithoutPositions(Turn turn);
    boolean invitedExists(Turn turn);
    List<User> getModeratorsOfTurn(long turnId);
    void deleteMemberWith(Long id);
}
