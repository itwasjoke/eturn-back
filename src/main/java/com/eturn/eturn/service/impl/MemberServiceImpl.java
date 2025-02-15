package com.eturn.eturn.service.impl;

import com.eturn.eturn.additionalService.member.MemberAccessService;
import com.eturn.eturn.additionalService.member.MemberNotificationService;
import com.eturn.eturn.additionalService.member.MemberRepositoryService;
import com.eturn.eturn.additionalService.member.MemberStatusService;
import com.eturn.eturn.dto.MemberDTO;
import com.eturn.eturn.dto.MemberListDTO;
import com.eturn.eturn.dto.mapper.MemberListMapper;
import com.eturn.eturn.entity.*;
import com.eturn.eturn.enums.*;
import com.eturn.eturn.exception.member.NoAccessMemberException;
import com.eturn.eturn.exception.member.NotFoundMemberException;
import com.eturn.eturn.exception.member.UnknownMemberException;
import com.eturn.eturn.repository.MemberRepository;
import com.eturn.eturn.service.*;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Работа с участниками очередей
 */
@Service
public class MemberServiceImpl implements MemberService {

    private final MemberRepositoryService mbrRepService;
    private final MemberAccessService mbrAccessService;
    private final MemberNotificationService mbrNotifyService;
    private final MemberStatusService mbrStatusService;
    private final MemberRepository memberRepository;
    private final MemberListMapper memberListMapper;
    private final UserService userService;
    private final TurnService turnService;

    public MemberServiceImpl(
            MemberRepositoryService mbrRepService,
            MemberAccessService mbrAccessService,
            MemberNotificationService mbrNotifyService,
            MemberStatusService mbrStatusService,
            MemberRepository memberRepository,
            MemberListMapper memberListMapper,
            UserService userService,
            TurnService turnService
            ) {
        this.mbrRepService = mbrRepService;
        this.mbrAccessService = mbrAccessService;
        this.mbrNotifyService = mbrNotifyService;
        this.mbrStatusService = mbrStatusService;
        this.memberRepository = memberRepository;
        this.memberListMapper = memberListMapper;
        this.userService = userService;
        this.turnService = turnService;
    }

    /**
     * Создание участника
     * @param user пользователь, который вступает в очередь
     * @param turn сама очередь
     * @param access тип доступа, который получает пользователь
     * @param invitedForTurn Нужно ли поставить статус приглашенного для данной очереди
     * @return участник
     */
    @Transactional
    @Override
    public Member createMember(
            User user,
            Turn turn,
            String access,
            boolean invitedForTurn
    ) {
        Optional<Member> memberOptional =
                memberRepository.findMemberByUserAndTurn(user,turn);
        if (memberOptional.isPresent()){
            throw new UnknownMemberException("this member already exists");
        }
        InvitedStatus status;
        AccessMember accessMember = AccessMember.valueOf(access);
        if (invitedForTurn) {
            status = InvitedStatus.INVITED;
        } else if (accessMember == AccessMember.MEMBER_LINK) {
            // если участник не стоит сейчас в очереди, то определяется формат доступа
            // по допустимым группам и факультетам
            if (turn.getAccessTurnType() ==
                    AccessTurn.FOR_ALLOWED_ELEMENTS) {
                Set<Group> groups = turn.getAllowedGroups();
                Set<Faculty> faculties = turn.getAllowedFaculties();
                if (
                        groups.contains(user.getGroup())
                                || faculties.contains(user.getGroup().getFaculty())
                ) {
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



    /**
     * Получение типа доступа к очереди
     * @param user пользователь
     * @param turn очередь
     * @return Тип доступа
     */
    @Override
    public AccessMember getAccess(User user, Turn turn) {
        Optional<Member> m = memberRepository.findMemberByUserAndTurn(user, turn);
        return m.map(Member::getAccessMember).orElse(null);
    }


    /**
     * Получение участника в формате представления
     * @param user пользователь
     * @param turn очередь
     * @return DTO участника
     */
    @Override
    public MemberDTO getMemberDTO(User user, Turn turn) {
        Optional<Member> member =
                memberRepository.findMemberByUserAndTurn(user, turn);
        if (member.isPresent()){
            Member memberToDTO = member.get();
            return new MemberDTO(
                    memberToDTO.getId(),
                    memberToDTO.getUser().getId(),
                    memberToDTO.getTurn().getId(),
                    memberToDTO.getUser().getName(),
                    memberToDTO.getUser().getGroup().getNumber(),
                    memberToDTO.getAccessMember().toString(),
                    memberToDTO.isInvitedForModerator(),
                    memberToDTO.getInvitedForTurn().toString()
            );
        }
        else{
            throw new NotFoundMemberException(
                    "Cannot find member on getMember method MemberServiceImpl.java"
            );
        }
    }

    /**
     * Блокировка/разблокировка пользователя
     * @param id идентификатор
     * @param access тип участника
     * @param username имя пользователя
     */
    @Override
    @Transactional
    public void setBlockStatus(
            long id,
            String access,
            String username
    ) {
        User user = userService.getUserFromLogin(username);
        // Проверка наличия участника
        Member memberToUpdate = memberRepository.findById(id)
                .orElseThrow(() -> new NotFoundMemberException(
                        "no member what you want to update"
                ));

        // Проверка прав текущего пользователя
        Member currentUserMember =
                memberRepository.findMemberByUserAndTurn(
                        user,
                        memberToUpdate.getTurn()
                )
                .orElseThrow(() -> new NotFoundMemberException(
                        "can't change member status because your member not found"
                ));

        // Проверка доступа текущего пользователя
        mbrAccessService.validateAccess(currentUserMember, memberToUpdate, access);

        // Обработка изменения статуса
        AccessMember newAccessMember = AccessMember.valueOf(access);
        if (mbrStatusService.handleStatusChange(memberToUpdate, newAccessMember)) {
            memberRepository.save(memberToUpdate);
        }
    }



    /**
     * Удаление участников без позиций
     * @param turn по очереди
     */
    @Override
    public void deleteMembersWithoutPositions(Turn turn) {
        memberRepository.deleteMembersWithoutPositions(turn);
    }

    /**
     * Подтверждение или отказ заявки участника
     * @param id идентификатор
     * @param status одобрено/отказано
     * @param isModerator модератор/участник
     */
    @Override
    public void changeMemberInvite(
            Long id,
            boolean status,
            boolean isModerator
    ) {
        Member member = mbrRepService.getMemberWith(id)
                .orElseThrow(() -> new NotFoundMemberException("Member not found"));

        if (status) {
            mbrStatusService.handleInviteActivation(member, isModerator);
        } else {
            mbrStatusService.handleInviteDeactivation(member, isModerator);
        }
    }

    /**
     * Проверка на существование заявок
     * @param turn очередь
     * @return существует/нет
     */
    @Override
    public boolean invitedExists(Turn turn) {
        return memberRepository.getOneInvitedExists(
                turn,
                true,
                InvitedStatus.INVITED
        ).isPresent();
    }

    /**
     * Получение списка модераторов
     * @param turnId идентификатор очереди
     * @return список пользователей
     */
    @Override
    public List<User> getModeratorsOfTurn(long turnId) {
        List<Member> members =
                memberRepository.getAllByTurn_IdAndAccessMember(
                        turnId,
                        AccessMember.MODERATOR
                );
        Member creator
                = memberRepository.getMemberByTurn_IdAndAccessMember(
                turnId,
                AccessMember.CREATOR
        );
        List<User> users = members.stream()
                .map(Member::getUser)
                .collect(Collectors.toList());

        users.add(creator.getUser());
        return users;
    }

    @Override
    public MemberListDTO getUnconfirmedMemberList(
            String username,
            String type,
            String hash
    ) {
        return mbrRepService.getUnconfirmedMemberList(
                username,
                type,
                hash);
    }


    /**
     * Приглашение пользователя на модерацию
     * @param hash хэш очереди
     * @param username имя пользователя
     */
    @Override
    @Transactional
    public void setInviteForMember(String hash, String username) {
        User user = userService.getUserFromLogin(username);
        Turn turn = turnService.getTurnFrom(hash);

        // Проверяем условия для приглашения
        // (ограничения на количество участников и проверка, что пользователь не создатель)
        mbrAccessService.validateInviteConditions(user, turn);

        // Получаем существующего участника или создаем нового, если он не существует
        Member member = getOrCreateMember(user, turn);

        // Обновляем статус участника, чтобы добавить приглашение
        mbrStatusService.updateMemberInviteStatus(member);

        // Отправляем уведомление о запросе на участие
        mbrNotifyService.notifyReceiptRequest(turn);
    }

    /**
     * Получает существующего участника или создает нового, если он не существует.
     * Если участник заблокирован, выбрасывает исключение.
     */
    private Member getOrCreateMember(User user, Turn turn) {
        return mbrRepService.getMemberWith(user, turn)
                .map(member -> {
                    // Если участник заблокирован, выбрасываем исключение
                    if (member.getAccessMember() == AccessMember.BLOCKED) {
                        throw new NoAccessMemberException("You are blocked");
                    }
                    return member;
                })
                .orElseGet(() -> createMember(
                        user,
                        turn,
                        "MEMBER_LINK",
                        false
                )); // Создаем нового участника, если он не существует
    }



    /**
     * Получение списка участников
     * @param username имя текущего пользователя
     * @param type участник/модератор/блок
     * @param hash хэш очереди
     * @param page страница
     * @return список участников
     */
    @Override
    public MemberListDTO getMemberList(
            String username,
            String type,
            String hash,
            int page
    ) {
        User user = userService.getUserFromLogin(username);
        Turn turn = turnService.getTurnFrom(hash);

        // Проверяем, есть ли у пользователя доступ к списку участников
        mbrAccessService.validateMemberAccess(user, turn);

        // Получаем список участников и их количество
        AccessMember accessMember = AccessMember.valueOf(type);
        Page<Member> members = mbrRepService.getMembersByAccess(turn, accessMember, page);
        long count = mbrRepService.getMemberCountByAccess(turn, accessMember);

        // Преобразуем результат в DTO и возвращаем
        return new MemberListDTO(memberListMapper.map(members), count);
    }




}
