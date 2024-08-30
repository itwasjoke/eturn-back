package com.eturn.eturn.service;

import com.eturn.eturn.dto.MemberDTO;
import com.eturn.eturn.entity.Member;
import com.eturn.eturn.entity.Turn;
import com.eturn.eturn.entity.User;
import com.eturn.eturn.enums.AccessMember;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;


public interface MemberService {
    void inviteMember(String hash, String username);
    Member createMember(User user, Turn turn, String access, boolean invitedForTurn);
    Optional<Member> getMemberWith(long id);
    Optional<Member> getMemberWith(User user, Turn turn);
    MemberDTO getMemberDTO(User user, Turn turn);
    AccessMember getAccess(User user, Turn turn);
    long getCountMembers(Turn turn);
    long getCountModerators(Turn turn);
    int countInviteModerators(Turn turn);
    int countInviteMembers(Turn turn);
    long countBlocked(Turn turn);
    List<MemberDTO> getMemberList(String username, String type, String hash, int page);
    List<MemberDTO> getUnconfirmedMemberList(String username, String type, String hash);
    Member changeMemberStatus(long id, String type, String username);
    void changeMemberStatusFrom(long id, String type, int invitedModerator, int invitedTurn);
    void changeMemberInvite(Long id, boolean status, boolean isModerator);
    void deleteMemberWith(Turn turn, User user);
    void deleteMembersWithoutPositions(Turn turn);
    void changeMemberInvite(Long id, boolean status);
    boolean invitedExists(Turn turn);
    List<User> getModeratorsOfTurn(long turnId);
    void deleteMemberWith(Long id);
}
