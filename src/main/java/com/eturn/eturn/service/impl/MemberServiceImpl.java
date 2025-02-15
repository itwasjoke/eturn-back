package com.eturn.eturn.service.impl;

import com.eturn.eturn.dto.MemberDTO;
import com.eturn.eturn.dto.MemberListDTO;
import com.eturn.eturn.dto.mapper.MemberListMapper;
import com.eturn.eturn.entity.*;
import com.eturn.eturn.enums.*;
import com.eturn.eturn.exception.member.NoAccessMemberException;
import com.eturn.eturn.exception.member.NotFoundMemberException;
import com.eturn.eturn.exception.member.UnknownMemberException;
import com.eturn.eturn.exception.position.NoInviteException;
import com.eturn.eturn.notifications.NotificationController;
import com.eturn.eturn.repository.MemberRepository;
import com.eturn.eturn.service.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.eturn.eturn.enums.AccessMember.BLOCKED;
import static com.eturn.eturn.enums.ChangeMbrAction.ADD_INVITE_STATUS;
import static com.eturn.eturn.enums.MemberListType.INVITED_MODERATOR;
import static com.eturn.eturn.enums.MemberListType.MODERATOR;

/**
 * Работа с участниками очередей
 */
@Service
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;
    private final MemberListMapper memberListMapper;
    private final UserService userService;
    private final PositionService positionService;
    private final TurnService turnService;
    private final NotificationController notificationController;
    public MemberServiceImpl(
            MemberRepository memberRepository,
            MemberListMapper memberListMapper,
            UserService userService,
            PositionService positionService,
            TurnService turnService,
            NotificationController notificationController
    ) {
        this.memberRepository = memberRepository;
        this.memberListMapper = memberListMapper;
        this.userService = userService;
        this.positionService = positionService;
        this.turnService = turnService;
        this.notificationController = notificationController;
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
     * Получение опционального участника по индексу
     * @param id идентификатор
     * @return участник
     */
    @Override
    public Optional<Member> getMemberWith(long id) {
        return memberRepository.findById(id);
    }

    /**
     * Получение опционального участника по пользователю и очереди
     * @param user пользователь
     * @param turn очередь
     * @return участник
     */
    @Override
    public Optional<Member> getMemberWith(User user, Turn turn) {
        return memberRepository.findMemberByUserAndTurn(user, turn);
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
     * Получение количества участников
     * @param turn очередь
     * @param memberListType тип участников
     * @return количество
     */
    @Override
    public int getCountMembersWith(
            Turn turn,
            MemberListType memberListType
    ) {
        switch (memberListType){
            case MEMBER -> {
                return memberRepository.countByTurnAndAccessMember(
                        turn,
                        AccessMember.MEMBER
                );
            }
            case MODERATOR -> {
                return memberRepository.countByTurnAndAccessMember(
                        turn,
                        AccessMember.MODERATOR
                );
            }
            case INVITED_MEMBER -> {
                return memberRepository.countByTurnAndInvitedForTurn(
                        turn,
                        InvitedStatus.INVITED
                );
            }
            case INVITED_MODERATOR -> {
                return memberRepository.countByTurnAndInvitedForModerator(
                        turn,
                        true
                );
            }
            case BLOCKED -> {
                return memberRepository.countByTurnAndAccessMember(
                        turn,
                        BLOCKED
                );
            }
            default -> {
                return 0;
            }
        }
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
        validateAccess(currentUserMember, memberToUpdate, access);

        // Обработка изменения статуса
        AccessMember newAccessMember = AccessMember.valueOf(access);
        if (handleStatusChange(memberToUpdate, newAccessMember)) {
            memberRepository.save(memberToUpdate);
        }
    }

    /**
     * Изменение составляющих очереди в зависимости от смены статуса
     * @param member участник
     * @param newAccessMember новый статус участника
     * @return true в случае сохранения пользователя, false в случае удаления
     */
    private boolean handleStatusChange(
            Member member,
            AccessMember newAccessMember
    ) {
        AccessMember currentAccessMember = member.getAccessMember();
        InvitedStatus invitedStatus = InvitedStatus.ACCESS_OUT;

        // Если мы разблокируем пользователя
        if (
                currentAccessMember == BLOCKED
                && newAccessMember == AccessMember.MEMBER
        ) {
            AccessTurn accessType = member.getTurn().getAccessTurnType();
            if (accessType == AccessTurn.FOR_ALLOWED_ELEMENTS) {
                deleteMemberWith(member.getId());
                return false;
            } else {
                invitedStatus = InvitedStatus.ACCESS_IN;
                newAccessMember = AccessMember.MEMBER_LINK;
            }

        // Если мы блокируем пользователя
        } else if (
                currentAccessMember == AccessMember.MEMBER
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
    private void updateMemberStatus(Member member, AccessMember accessMember, InvitedStatus invitedStatus) {
        if (accessMember == BLOCKED) {
            member.setInvitedForTurn(invitedStatus);

        } else {
            member.setInvitedForTurn(invitedStatus);
        }
        member.setInvitedForModerator(false);
        member.setAccessMember(accessMember);
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
        Optional<Member> member = memberRepository.findById(id);
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
                    case ADD_ACCESS_TURN -> memberGet.setInvitedForTurn(InvitedStatus.ACCESS_IN);
                    case ADD_INVITE_STATUS -> memberGet.setInvitedForTurn(InvitedStatus.INVITED);
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

    /**
     * Удаление участника
     * @param turn по очереди
     * @param user и по пользователю
     */
    @Override
    public void deleteMemberWith(Turn turn, User user) {
        memberRepository.deleteByTurnAndUser(turn, user);
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
     * Удалить участника
     * @param id идентификатор
     */
    @Override
    public void deleteMemberWith(Long id) {
        memberRepository.deleteById(id);
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
        Member member = getMemberWith(id)
                .orElseThrow(() -> new NotFoundMemberException("Member not found"));

        if (status) {
            handleInviteActivation(member, isModerator);
        } else {
            handleInviteDeactivation(member, isModerator);
        }
    }

    /**
     * Подтверждение заявки
     * @param member участник
     * @param isModerator модератор/участник
     */
    private void handleInviteActivation(Member member, boolean isModerator) {
        boolean isInvitedForModerator = member.isInvitedForModerator();
        InvitedStatus invitedStatusForTurn = member.getInvitedForTurn();

        // подтверждение модератора
        if (isInvitedForModerator && isModerator) {
            changeMemberStatusFrom(
                    member.getId(),
                    "MODERATOR",
                    Optional.of(ChangeMbrAction.DELETE_INVITE_STATUS),
                    Optional.of(ChangeMbrAction.ADD_ACCESS_TURN)
            );
            if (invitedStatusForTurn == InvitedStatus.INVITED){
                positionService.createPositionAndSave(
                        member.getUser().getLogin(),
                        member.getTurn().getHash()
                );
            }
        // подтверждение заявки участника
        } else if (invitedStatusForTurn == InvitedStatus.INVITED && !isModerator) {
            changeMemberStatusFrom(
                    member.getId(),
                    "MEMBER",
                    Optional.empty(),
                    Optional.of(ChangeMbrAction.ADD_ACCESS_TURN)
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
    private void handleInviteDeactivation(Member member, boolean isModerator) {
        boolean invitedForModerator = member.isInvitedForModerator();
        InvitedStatus invitedForTurn = member.getInvitedForTurn();

        // отказ от модератора
        if (invitedForModerator && isModerator) {
            changeMemberStatusFrom(
                    member.getId(),
                    null,
                    Optional.of(ChangeMbrAction.DELETE_INVITE_STATUS),
                    Optional.empty()
            );

        // отказ от участника
        } else if (invitedForTurn == InvitedStatus.INVITED && !isModerator) {
            if (invitedForModerator) {
                changeMemberStatusFrom(
                        member.getId(),
                        "MEMBER_LINK",
                        Optional.empty(),
                        Optional.of(ADD_INVITE_STATUS)
                );
            } else {
                deleteMemberWith(member.getId());
            }
        } else {
            throw new NoInviteException("Member is not invited");
        }
    }

    /**
     * Проверка на существование заявок
     * @param turn очередь
     * @return существует/нет
     */
    @Override
    public boolean invitedExists(Turn turn) {
        return memberRepository.getOneInvitedExists(turn, true, InvitedStatus.INVITED).isPresent();
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
        validateInviteConditions(user, turn);

        // Получаем существующего участника или создаем нового, если он не существует
        Member member = getOrCreateMember(user, turn);

        // Обновляем статус участника, чтобы добавить приглашение
        updateMemberInviteStatus(member);

        // Отправляем уведомление о запросе на участие
        notifyReceiptRequest(turn);
    }

    /**
     * Проверяет, превышен ли лимит приглашенных модераторов или модераторов в очереди.
     */
    private boolean isInviteLimitExceeded(Turn turn) {
        return getCountMembersWith(turn, MemberListType.INVITED_MODERATOR) > 20
                || getCountMembersWith(turn, MemberListType.MODERATOR) > 20;
    }

    /**
     * Проверяет, является ли пользователь создателем очереди.
     */
    private boolean isUserCreator(User user, Turn turn) {
        return turn.getCreator() == user;
    }

    /**
     * Получает существующего участника или создает нового, если он не существует.
     * Если участник заблокирован, выбрасывает исключение.
     */
    private Member getOrCreateMember(User user, Turn turn) {
        return getMemberWith(user, turn)
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
     * Обновляет статус участника, чтобы добавить приглашение.
     */
    private void updateMemberInviteStatus(Member member) {
        changeMemberStatusFrom(
                member.getId(),
                null,
                Optional.of(ChangeMbrAction.ADD_INVITE_STATUS),
                Optional.empty()
        );
    }

    /**
     * Отправляет уведомление о запросе на участие в очереди.
     */
    private void notifyReceiptRequest(Turn turn) {
        notificationController.notifyReceiptRequest(turn.getId(), turn.getName());
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
        validateMemberAccess(user, turn);

        // Получаем список участников и их количество
        AccessMember accessMember = AccessMember.valueOf(type);
        Page<Member> members = getMembersByAccess(turn, accessMember, page);
        long count = getMemberCountByAccess(turn, accessMember);

        // Преобразуем результат в DTO и возвращаем
        return new MemberListDTO(memberListMapper.map(members), count);
    }

    /**
     * Проверяет, есть ли у пользователя доступ к списку участников.
     * Если пользователь не является создателем или модератором, выбрасывает исключение.
     */
    private void validateMemberAccess(User user, Turn turn) {
        Member member = getMemberWith(user, turn)
                .orElseThrow(() -> new NotFoundMemberException("no member"));

        if (
                member.getAccessMember() != AccessMember.CREATOR
                && member.getAccessMember() != AccessMember.MODERATOR
        ) {
            throw new NoAccessMemberException("No access");
        }
    }

    /**
     * Получает список участников по типу доступа с пагинацией.
     */
    private Page<Member> getMembersByAccess(
            Turn turn,
            AccessMember accessMember,
            int page
    ) {
        Pageable paging = PageRequest.of(page, 20);
        return memberRepository.getMemberByTurnAndAccessMember(
                turn,
                accessMember,
                paging
        );
    }

    /**
     * Получает количество участников по типу доступа.
     */
    private long getMemberCountByAccess(Turn turn, AccessMember accessMember) {
        return memberRepository.countByTurnAndAccessMember(turn, accessMember);
    }

    /**
     * Получение списка неподтвержденных участников
     * @param username имя текущего пользователя
     * @param type модератор/участник
     * @param hash хэш очереди
     * @return список участников
     */
    @Override
    public MemberListDTO getUnconfirmedMemberList(
            String username,
            String type,
            String hash
    ) {
        // Получаем пользователя и очередь
        User user = userService.getUserFromLogin(username);
        Turn turn = turnService.getTurnFrom(hash);

        // Проверяем, есть ли у пользователя доступ к списку неподтвержденных участников
        validateMemberForListAccess(user, turn);

        // Получаем список неподтвержденных участников и их количество
        AccessMember accessMember = AccessMember.valueOf(type);
        List<Member> members = getUnconfirmedMembers(turn, accessMember);
        long count = getUnconfirmedMemberCount(turn, accessMember);

        // Преобразуем результат в DTO и возвращаем
        return new MemberListDTO(memberListMapper.mapMember(members), count);
    }

    /**
     * Проверяет, есть ли у пользователя доступ к списку неподтвержденных участников.
     * Если пользователь не является создателем или модератором, выбрасывает исключение.
     */
    private void validateMemberForListAccess(User user, Turn turn) {
        Member member = getMemberWith(user, turn)
                .orElseThrow(() -> new NotFoundMemberException("no member"));

        if (
                member.getAccessMember() != AccessMember.CREATOR
                && member.getAccessMember() != AccessMember.MODERATOR
        ) {
            throw new NoAccessMemberException("No access");
        }
    }

    /**
     * Получает список неподтвержденных участников в зависимости от типа доступа.
     */
    private List<Member> getUnconfirmedMembers(
            Turn turn,
            AccessMember accessMember
    ) {
        if (accessMember == AccessMember.MODERATOR) {
            return memberRepository
                    .getMemberByTurnAndInvitedForModerator(turn, true);
        } else if (accessMember == AccessMember.MEMBER) {
            Pageable paging = PageRequest.of(0, 20);
            Page<Member> page = memberRepository
                    .getMemberByTurnAndAccessMemberAndInvitedForTurn(
                    turn,
                    AccessMember.MEMBER_LINK,
                    InvitedStatus.INVITED,
                    paging
            );
            return page.toList();
        }
        return Collections.emptyList(); // Возвращаем пустой список, если тип доступа не подходит
    }

    /**
     * Получает количество неподтвержденных участников в зависимости от типа доступа.
     */
    private long getUnconfirmedMemberCount(
            Turn turn, AccessMember accessMember
    ) {
        if (accessMember == AccessMember.MODERATOR) {
            return memberRepository
                    .countByTurnAndInvitedForModerator(turn, true);
        } else if (accessMember == AccessMember.MEMBER) {
            return memberRepository.countByTurnAndInvitedForTurn(
                    turn, InvitedStatus.INVITED
            );
        }
        return 0L; // Возвращаем 0, если тип доступа не подходит
    }
}
