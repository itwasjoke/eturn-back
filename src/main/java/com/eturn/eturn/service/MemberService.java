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
    void setInviteForMember(
            String hash,
            String username
    );
    MemberDTO getMemberDTO(
            User user,
            Turn turn
    );
    MemberListDTO getMemberList(
            String username,
            String type,
            String hash,
            int page
    );
    void setBlockStatus(
            long id,
            String type,
            String username
    );
    void changeMemberInvite(
            Long id,
            boolean status,
            boolean isModerator
    );
    void deleteMembersWithoutPositions(Turn turn);
    boolean invitedExists(Turn turn);

    MemberListDTO getUnconfirmedMemberList(
            String username,
            String type,
            String hash
    );
    Member createMember(
            User user,
            Turn turn,
            String access,
            boolean invitedForTurn
    );
    List<User> getModeratorsOfTurn(long turnId);
}
