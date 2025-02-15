package com.eturn.eturn.additionalService.member.impl;

import com.eturn.eturn.additionalService.member.MemberAccessService;
import com.eturn.eturn.entity.Member;
import com.eturn.eturn.entity.Turn;
import com.eturn.eturn.entity.User;
import com.eturn.eturn.enums.AccessMember;
import com.eturn.eturn.enums.MemberListType;
import com.eturn.eturn.exception.member.NoAccessMemberException;
import com.eturn.eturn.exception.position.NoInviteException;

import java.util.Set;

public class MemberAccessServiceImpl implements MemberAccessService {
    /**
     * Валидация доступа к изменению статусов
     * @param currentUserMember текущий участник
     * @param targetMember цель для изменения
     * @param type изменяемый тип
     */
    private void validateAccess(
            Member currentUserMember,
            Member targetMember,
            String type
    ) {
        // Проверка, что текущий пользователь имеет права на изменение
        if (currentUserMember.getAccessMember() != AccessMember.MODERATOR &&
                currentUserMember.getAccessMember() != AccessMember.CREATOR) {
            throw new NoAccessMemberException("you don't have root for this operation");
        }

        // Проверка, что целевой участник не является создателем
        if (targetMember.getAccessMember() == AccessMember.CREATOR) {
            throw new NoAccessMemberException("you are creator");
        }

        // Проверка допустимых значений типа
        if (!Set.of("MEMBER", "BLOCKED").contains(type)) {
            throw new NoAccessMemberException("no access");
        }
    }

    /**
     * Проверяет условия для приглашения:
     * 1. Не превышен ли лимит приглашенных модераторов или модераторов.
     * 2. Является ли пользователь создателем очереди.
     */
    private void validateInviteConditions(User user, Turn turn) {
        if (isInviteLimitExceeded(turn)) {
            throw new NoInviteException("You can't be invited");
        }
        if (isUserCreator(user, turn)) {
            throw new NoAccessMemberException("You are creator");
        }
    }
    /**
     * Проверяет, превышен ли лимит приглашенных модераторов или модераторов в очереди.
     */
    private boolean isInviteLimitExceeded(Turn turn) {
        return getCountMembersWith(turn, MemberListType.INVITED_MODERATOR) > 20
                || getCountMembersWith(turn, MemberListType.MODERATOR) > 20;
    }
}
