package com.eturn.eturn.additionalService.member;

import com.eturn.eturn.entity.Member;
import com.eturn.eturn.enums.AccessMember;
import com.eturn.eturn.enums.ChangeMbrAction;
import com.eturn.eturn.enums.InvitedStatus;

import java.util.Optional;

public interface MemberStatusService {
    boolean handleStatusChange(Member member, AccessMember newAccessMember);
    void updateMemberStatus(Member member, AccessMember accessMember, InvitedStatus invitedStatus);
    void handleInviteActivation(Member member, boolean isModerator);
    void handleInviteDeactivation(Member member, boolean isModerator);
    void updateMemberInviteStatus(Member member);
    void changeMemberStatusFrom(
            long id,
            String type,
            Optional<ChangeMbrAction> actionMod,
            Optional<ChangeMbrAction> actionTurn
    );
}
