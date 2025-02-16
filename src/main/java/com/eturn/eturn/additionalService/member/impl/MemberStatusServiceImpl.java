package com.eturn.eturn.additionalService.member.impl;

import com.eturn.eturn.additionalService.member.MemberRepositoryService;
import com.eturn.eturn.additionalService.member.MemberStatusService;
import com.eturn.eturn.entity.Member;
import com.eturn.eturn.enums.AccessMember;
import com.eturn.eturn.enums.AccessTurn;
import com.eturn.eturn.enums.ChangeMbrAction;
import com.eturn.eturn.enums.InvitedStatus;
import com.eturn.eturn.exception.member.NotFoundMemberException;
import com.eturn.eturn.exception.position.NoInviteException;
import com.eturn.eturn.repository.MemberRepository;
import com.eturn.eturn.service.PositionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.eturn.eturn.enums.AccessMember.*;
import static com.eturn.eturn.enums.AccessTurn.FOR_ALLOWED_ELEMENTS;
import static com.eturn.eturn.enums.ChangeMbrAction.*;
import static com.eturn.eturn.enums.InvitedStatus.*;

@Service
public class MemberStatusServiceImpl implements MemberStatusService {



    private final MemberRepository memberRepository;
    private PositionService positionService;

    public MemberStatusServiceImpl(
            MemberRepository memberRepository
    ) {
        this.memberRepository = memberRepository;
    }

    @Autowired
    public void setPositionService(PositionService positionService){
        this.positionService = positionService;
    }

    /**
     * Изменение составляющих очереди в зависимости от смены статуса
     * @param member участник
     * @param newAccessMember новый статус участника
     * @return true в случае сохранения пользователя, false в случае удаления
     */
    public boolean handleStatusChange(
            Member member,
            AccessMember newAccessMember
    ) {
        AccessMember currentAccessMember = member.getAccessMember();
        InvitedStatus invitedStatus = ACCESS_OUT;

        // Если мы разблокируем пользователя
        if (
                currentAccessMember == BLOCKED
                        && newAccessMember == MEMBER
        ) {
            AccessTurn accessType = member.getTurn().getAccessTurnType();
            if (accessType == FOR_ALLOWED_ELEMENTS) {
                memberRepository.deleteById(member.getId());
                return false;
            } else {
                invitedStatus = ACCESS_IN;
                newAccessMember = MEMBER_LINK;
            }

            // Если мы блокируем пользователя
        } else if (
                currentAccessMember == MEMBER
                        && newAccessMember == BLOCKED
        ) {
            positionService.deleteAllByTurnAndUser(member.getTurn(), member.getUser());
        }

        updateMemberStatus(member, newAccessMember, invitedStatus);
        return true;
    }

    /**
     * Изменение всех статусов участника
     * @param member участник
     * @param accessMember статус доступа
     * @param invitedStatus статус приглашения
     */
    public void updateMemberStatus(Member member, AccessMember accessMember, InvitedStatus invitedStatus) {
        if (accessMember == BLOCKED) {
            member.setInvitedForTurn(invitedStatus);

        } else {
            member.setInvitedForTurn(invitedStatus);
        }
        member.setInvitedForModerator(false);
        member.setAccessMember(accessMember);
    }

    /**
     * Подтверждение заявки
     * @param member участник
     * @param isModerator модератор/участник
     */
    public void handleInviteActivation(Member member, boolean isModerator) {
        boolean isInvitedForModerator = member.isInvitedForModerator();
        InvitedStatus invitedStatusForTurn = member.getInvitedForTurn();

        // подтверждение модератора
        if (isInvitedForModerator && isModerator) {
            changeMemberStatusFrom(
                    member.getId(),
                    "MODERATOR",
                    Optional.of(DELETE_INVITE_STATUS),
                    Optional.of(ADD_ACCESS_TURN)
            );
            if (invitedStatusForTurn == INVITED){
                positionService.createPositionAndSave(
                        member.getUser().getLogin(),
                        member.getTurn().getHash()
                );
            }
            // подтверждение заявки участника
        } else if (invitedStatusForTurn == INVITED && !isModerator) {
            changeMemberStatusFrom(
                    member.getId(),
                    "MEMBER",
                    Optional.empty(),
                    Optional.of(ADD_ACCESS_TURN)
            );
            positionService.createPositionAndSave(
                    member.getUser().getLogin(),
                    member.getTurn().getHash()
            );
        } else {
            throw new NoInviteException("Member is not invited");
        }
    }

    /**
     * Отмена заявки
     * @param member участника
     * @param isModerator модератор/участник
     */
    public void handleInviteDeactivation(Member member, boolean isModerator) {
        boolean invitedForModerator = member.isInvitedForModerator();
        InvitedStatus invitedForTurn = member.getInvitedForTurn();

        // отказ от модератора
        if (invitedForModerator && isModerator) {
            changeMemberStatusFrom(
                    member.getId(),
                    null,
                    Optional.of(DELETE_INVITE_STATUS),
                    Optional.empty()
            );

            // отказ от участника
        } else if (invitedForTurn == INVITED && !isModerator) {
            if (invitedForModerator) {
                changeMemberStatusFrom(
                        member.getId(),
                        "MEMBER_LINK",
                        Optional.empty(),
                        Optional.of(ADD_INVITE_STATUS)
                );
            } else {
                memberRepository.deleteById(member.getId());
            }
        } else {
            throw new NoInviteException("Member is not invited");
        }
    }

    /**
     * Обновляет статус участника, чтобы добавить приглашение.
     */
    public void updateMemberInviteStatus(Member member) {
        changeMemberStatusFrom(
                member.getId(),
                null,
                Optional.of(ADD_INVITE_STATUS),
                Optional.empty()
        );
    }

    /**
     * Изменение статуса во время других действий
     * @param id идентификатор
     * @param type тип доступа
     * @param actionMod изменение статуса приглашения модератора
     * @param actionTurn иземенение доступа к очереди
     */
    @Override
    public void changeMemberStatusFrom(
            long id,
            String type,
            Optional<ChangeMbrAction> actionMod,
            Optional<ChangeMbrAction> actionTurn
    ) {
        Optional<Member> member = memberRepository.getMemberById(id);
        if (member.isPresent()) {
            Member memberGet = member.get();

            // Установка доступов для модерации
            if (actionMod.isPresent()) {
                switch (actionMod.get()) {
                    case ADD_INVITE_STATUS -> memberGet.setInvitedForModerator(true);
                    case DELETE_INVITE_STATUS -> memberGet.setInvitedForModerator(false);
                }
            }
            // Установка доступов для очереди
            if (actionTurn.isPresent()) {
                switch (actionTurn.get()){
                    case ADD_ACCESS_TURN -> memberGet.setInvitedForTurn(ACCESS_IN);
                    case ADD_INVITE_STATUS -> memberGet.setInvitedForTurn(INVITED);
                }
            }
            if (type != null) {
                AccessMember accessMember = AccessMember.valueOf(type);
                memberGet.setAccessMember(accessMember);
            }
            memberRepository.save(memberGet);
        }
        else{
            throw new NotFoundMemberException("no member what you want to update");
        }
    }
}
