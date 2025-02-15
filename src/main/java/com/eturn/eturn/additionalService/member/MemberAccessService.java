package com.eturn.eturn.additionalService.member;

import com.eturn.eturn.entity.Member;
import com.eturn.eturn.entity.Turn;
import com.eturn.eturn.entity.User;

public interface MemberAccessService {
    void validateAccess(Member currentUserMember, Member targetMember, String type);
    void validateInviteConditions(User user, Turn turn);
    boolean isInviteLimitExceeded(Turn turn);
    boolean isUserCreator(User user, Turn turn);
    void validateMemberAccess(User user, Turn turn);
    void validateMemberForListAccess(User user, Turn turn);
}
