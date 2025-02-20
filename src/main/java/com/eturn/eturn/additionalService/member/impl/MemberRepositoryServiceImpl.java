package com.eturn.eturn.additionalService.member.impl;

import com.eturn.eturn.additionalService.member.MemberAccessService;
import com.eturn.eturn.additionalService.member.MemberRepositoryService;
import com.eturn.eturn.dto.MemberListDTO;
import com.eturn.eturn.dto.mapper.MemberListMapper;
import com.eturn.eturn.entity.*;
import com.eturn.eturn.enums.AccessMember;
import com.eturn.eturn.enums.InvitedStatus;
import com.eturn.eturn.enums.MemberListType;
import com.eturn.eturn.exception.member.NoAccessMemberException;
import com.eturn.eturn.exception.member.UnknownMemberException;
import com.eturn.eturn.repository.MemberRepository;
import com.eturn.eturn.service.TurnService;
import com.eturn.eturn.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.eturn.eturn.enums.AccessMember.*;
import static com.eturn.eturn.enums.AccessTurn.FOR_ALLOWED_ELEMENTS;
import static com.eturn.eturn.enums.InvitedStatus.*;

@Service
public class MemberRepositoryServiceImpl implements MemberRepositoryService {

    private final MemberRepository memberRepository;
    private final UserService userService;
    private TurnService turnService;
    private final MemberListMapper memberListMapper;

    private MemberAccessService memberAccessService;

    public MemberRepositoryServiceImpl(
            MemberRepository memberRepository,
            UserService userService,
            MemberListMapper memberListMapper
    ) {
        this.memberRepository = memberRepository;
        this.userService = userService;
        this.memberListMapper = memberListMapper;
    }

    @Autowired
    public void setTurnService(TurnService turnService){
        this.turnService = turnService;
    }

    @Autowired
    public void setMemberAccessService(MemberAccessService memberAccessService){
        this.memberAccessService = memberAccessService;
    }

    /**
     * Получает список участников по типу доступа с пагинацией.
     */
    public Page<Member> getMembersByAccess(
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
    public long getMemberCountByAccess(Turn turn, AccessMember accessMember) {
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
        memberAccessService.validateMemberForListAccess(user, turn);

        // Получаем список неподтвержденных участников и их количество
        AccessMember accessMember = AccessMember.valueOf(type);
        List<Member> members = getUnconfirmedMembers(turn, accessMember);
        long count = getUnconfirmedMemberCount(turn, accessMember);

        // Преобразуем результат в DTO и возвращаем
        return new MemberListDTO(memberListMapper.mapMember(members), count);
    }

    /**
     * Получает список неподтвержденных участников в зависимости от типа доступа.
     */
    public List<Member> getUnconfirmedMembers(
            Turn turn,
            AccessMember accessMember
    ) {
        if (accessMember == MODERATOR) {
            return memberRepository
                    .getMemberByTurnAndInvitedForModerator(turn, true);
        } else if (accessMember == MEMBER) {
            Pageable paging = PageRequest.of(0, 20);
            Page<Member> page = memberRepository
                    .getMemberByTurnAndAccessMemberAndInvitedForTurn(
                            turn,
                            MEMBER_LINK,
                            INVITED,
                            paging
                    );
            return page.toList();
        }
        return Collections.emptyList(); // Возвращаем пустой список, если тип доступа не подходит
    }

    /**
     * Получает количество неподтвержденных участников в зависимости от типа доступа.
     */
    public long getUnconfirmedMemberCount(
            Turn turn, AccessMember accessMember
    ) {
        if (accessMember == MODERATOR) {
            return memberRepository
                    .countByTurnAndInvitedForModerator(turn, true);
        } else if (accessMember == MEMBER) {
            return memberRepository.countByTurnAndInvitedForTurn(
                    turn, INVITED
            );
        }
        return 0L; // Возвращаем 0, если тип доступа не подходит
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
                        MEMBER
                );
            }
            case MODERATOR -> {
                return memberRepository.countByTurnAndAccessMember(
                        turn,
                        MODERATOR
                );
            }
            case INVITED_MEMBER -> {
                return memberRepository.countByTurnAndInvitedForTurn(
                        turn,
                        INVITED
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
     * Удаление участника
     * @param turn по очереди
     * @param user и по пользователю
     */
    @Override
    public void deleteMemberWith(Turn turn, User user) {
        memberRepository.deleteByTurnAndUser(turn, user);
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
     * Получает существующего участника или создает нового, если он не существует.
     * Если участник заблокирован, выбрасывает исключение.
     */
    public Member getOrCreateMember(User user, Turn turn) {
        return getMemberWith(user, turn)
                .map(member -> {
                    // Если участник заблокирован, выбрасываем исключение
                    if (member.getAccessMember() == BLOCKED) {
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
            status = INVITED;
        } else if (accessMember == MEMBER_LINK) {
            // если участник не стоит сейчас в очереди, то определяется формат доступа
            // по допустимым группам и факультетам
            if (turn.getAccessTurnType() ==
                    FOR_ALLOWED_ELEMENTS) {
                Set<Group> groups = turn.getAllowedGroups();
                Set<Faculty> faculties = turn.getAllowedFaculties();
                if (
                        groups.contains(user.getGroup())
                                || faculties.contains(user.getGroup().getFaculty())
                ) {
                    status = ACCESS_IN;
                } else {
                    status = ACCESS_OUT;
                }
            }
            else {
                status = ACCESS_OUT;
            }
        } else {
            status = ACCESS_IN;
        }
        Member member = new Member();
        member.setAccessMember(accessMember);
        member.setTurn(turn);
        member.setUser(user);
        member.setInvitedForTurn(status);
        return memberRepository.save(member);
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
                        MODERATOR
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
}
