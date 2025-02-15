package com.eturn.eturn.additionalService.member.impl;

import com.eturn.eturn.additionalService.member.MemberAccessService;
import com.eturn.eturn.additionalService.member.MemberRepositoryService;
import com.eturn.eturn.entity.Member;
import com.eturn.eturn.entity.Turn;
import com.eturn.eturn.entity.User;
import com.eturn.eturn.enums.AccessMember;
import com.eturn.eturn.enums.MemberListType;
import com.eturn.eturn.exception.member.NoAccessMemberException;
import com.eturn.eturn.exception.member.NotFoundMemberException;
import com.eturn.eturn.exception.position.NoInviteException;
import org.springframework.stereotype.Service;

import java.util.Set;
@Service
public class MemberAccessServiceImpl implements MemberAccessService {

    private final MemberRepositoryService mbrRepService;

    public MemberAccessServiceImpl(
            MemberRepositoryService mbrRepService
    ) {
        this.mbrRepService = mbrRepService;
    }

    /**
     * Валидация доступа к изменению статусов
     * @param currentUserMember текущий участник
     * @param targetMember цель для изменения
     * @param type изменяемый тип
     */
    public void validateAccess(
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
    public void validateInviteConditions(User user, Turn turn) {
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
    public boolean isInviteLimitExceeded(Turn turn) {
        return mbrRepService.getCountMembersWith(turn, MemberListType.INVITED_MODERATOR) > 20
                || mbrRepService.getCountMembersWith(turn, MemberListType.MODERATOR) > 20;
    }

    /**
     * Проверяет, является ли пользователь создателем очереди.
     */
    public boolean isUserCreator(User user, Turn turn) {
        return turn.getCreator() == user;
    }

    /**
     * Проверяет, есть ли у пользователя доступ к списку участников.
     * Если пользователь не является создателем или модератором, выбрасывает исключение.
     */
    public void validateMemberAccess(User user, Turn turn) {
        Member member = mbrRepService.getMemberWith(user, turn)
                .orElseThrow(() -> new NotFoundMemberException("no member"));

        if (
                member.getAccessMember() != AccessMember.CREATOR
                        && member.getAccessMember() != AccessMember.MODERATOR
        ) {
            throw new NoAccessMemberException("No access");
        }
    }

    /**
     * Проверяет, есть ли у пользователя доступ к списку неподтвержденных участников.
     * Если пользователь не является создателем или модератором, выбрасывает исключение.
     */
    public void validateMemberForListAccess(User user, Turn turn) {
        Member member = mbrRepService.getMemberWith(user, turn)
                .orElseThrow(() -> new NotFoundMemberException("no member"));

        if (
                member.getAccessMember() != AccessMember.CREATOR
                        && member.getAccessMember() != AccessMember.MODERATOR
        ) {
            throw new NoAccessMemberException("No access");
        }
    }
}
