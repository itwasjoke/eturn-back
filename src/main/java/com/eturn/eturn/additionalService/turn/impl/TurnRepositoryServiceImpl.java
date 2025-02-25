package com.eturn.eturn.additionalService.turn.impl;

import com.eturn.eturn.additionalService.member.MemberRepositoryService;
import com.eturn.eturn.additionalService.turn.TurnRepositoryService;
import com.eturn.eturn.additionalService.turn.impl.data.AccessInfo;
import com.eturn.eturn.additionalService.turn.impl.data.TurnDetails;
import com.eturn.eturn.dto.MembersCountDTO;
import com.eturn.eturn.dto.TurnDTO;
import com.eturn.eturn.dto.TurnForListDTO;
import com.eturn.eturn.dto.mapper.TurnForListMapper;
import com.eturn.eturn.dto.mapper.TurnMapper;
import com.eturn.eturn.entity.*;
import com.eturn.eturn.enums.AccessMember;
import com.eturn.eturn.exception.turn.LocalNotFoundTurnException;
import com.eturn.eturn.exception.turn.NotFoundAllTurnsException;
import com.eturn.eturn.exception.turn.NotFoundTurnException;
import com.eturn.eturn.repository.TurnRepository;
import com.eturn.eturn.service.MemberService;
import com.eturn.eturn.service.PositionService;
import com.eturn.eturn.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.eturn.eturn.enums.MemberListType.*;

@Service
public class TurnRepositoryServiceImpl implements TurnRepositoryService {

    private static final Logger logger = LogManager.getLogger(TurnRepositoryServiceImpl.class);

    private final TurnRepository turnRepository;
    private final UserService userService;
    private final MemberService memberService;
    private final PositionService positionService;
    private final MemberRepositoryService mbrRepService;
    private final TurnMapper turnMapper;
    private final TurnForListMapper turnForListMapper;

    public TurnRepositoryServiceImpl(
            TurnRepository turnRepository,
            UserService userService,
            MemberService memberService,
            PositionService positionService,
            MemberRepositoryService memberRepositoryService,
            TurnMapper turnMapper,
            TurnForListMapper turnForListMapper
    ) {
        this.turnRepository = turnRepository;
        this.userService = userService;
        this.memberService = memberService;
        this.positionService = positionService;
        mbrRepService = memberRepositoryService;
        this.turnMapper = turnMapper;
        this.turnForListMapper = turnForListMapper;
    }

    /**
     * Получаем текущую очередь
     * @param hash хэш очереди
     * @param login имя пользователя
     * @return очередь с подробной информацией
     */
    @Override
    public TurnDTO getTurn(String hash, String login) {
        // Получаем пользователя и очередь по хэшу
        User user = userService.getUserFromLogin(login);
        Turn turn = getTurnFrom(hash);

        // Проверяем, не устарела ли очередь
        validateTurnExpiration(turn);

        // Получаем информацию о члене (участнике) очереди
        Optional<Member> memberOptional =
                mbrRepService.getMemberWith(user, turn);

        // Собираем данные о доступе, приглашениях и количестве участников
        TurnDetails details = collectTurnDetails(
                memberOptional,
                turn
        );

        // Определяем тип доступа и список разрешенных групп/факультетов
        AccessInfo accessInfo = determineAccessInfo(turn);

        // Преобразуем данные в DTO и возвращаем результат
        return turnMapper.turnToTurnDTO(
                turn,
                details.getAccess(),
                accessInfo.getAccessType(),
                details.getInvitedForTurn(),
                details.isInvitedForModerator(),
                details.isExistsInvited(),
                details.getMembersCountDTO(),
                accessInfo.getAllowedIds(),
                details.getPositionsCount()
        );
    }

    private Turn getTurnFrom(String hash){
        Optional<Turn> turn = turnRepository.findTurnByHash(hash);
        if(turn.isPresent()){
            return turn.get();
        }
        else{
            throw new LocalNotFoundTurnException("No turn in database on getTurnFrom method (TurnServiceImpl.java)");
        }
    }

    /**
     * Проверяет, не устарела ли очередь. Если устарела, удаляет её и выбрасывает исключение.
     *
     * @param turn  Очередь для проверки.
     * @throws NotFoundTurnException Если очередь устарела и была удалена.
     */
    private void validateTurnExpiration(Turn turn) {
        if (turn.getDateEnd().getTime() < new Date().getTime()) {
            turnRepository.deleteTurnById(turn.getId());
            throw new NotFoundTurnException("Turn was deleted");
        }
    }

    /**
     * Собирает информацию о доступе, приглашениях и количестве участников.
     *
     * @param memberOptional Опциональный объект участника.
     * @param turn           Очередь, для которой собираются данные.
     * @return Объект TurnDetails с собранной информацией.
     */
    private TurnDetails collectTurnDetails(
            Optional<Member> memberOptional,
            Turn turn
    ) {
        TurnDetails details = new TurnDetails();
        logger.info("member exists: "+memberOptional.isPresent());
        if (memberOptional.isPresent()) {
            Member member = memberOptional.get();
            logger.info(member.getInvitedForTurn().toString()+" status of invitedForTurn");
            details.setAccess(
                    member.getAccessMember().name()
            );
            details.setInvitedForModerator(
                    member.isInvitedForModerator()
            );
            details.setInvitedForTurn(
                    member.getInvitedForTurn().toString()
            );

            if (
                    member.getAccessMember() == AccessMember.CREATOR ||
                    member.getAccessMember() == AccessMember.MODERATOR
            ) {
                details.setExistsInvited(memberService.invitedExists(turn));
                details.setMembersCountDTO(new MembersCountDTO(
                        mbrRepService.getCountMembersWith(turn, MODERATOR),
                        mbrRepService.getCountMembersWith(turn, MEMBER),
                        mbrRepService.getCountMembersWith(turn, INVITED_MEMBER),
                        mbrRepService.getCountMembersWith(turn, INVITED_MODERATOR),
                        mbrRepService.getCountMembersWith(turn, BLOCKED)
                ));
            }
        }

        details.setPositionsCount(
                positionService.countPositionsByTurn(turn)
        );
        return details;
    }

    /**
     * Определяет тип доступа и список разрешенных групп/факультетов.
     *
     * @param turn Очередь, для которой определяется доступ.
     * @return Объект AccessInfo с типом доступа и списком ID.
     */
    private AccessInfo determineAccessInfo(Turn turn) {
        AccessInfo accessInfo = new AccessInfo();
        List<Long> allowedIds = new ArrayList<>();

        if (!turn.getAllowedGroups().isEmpty()) {
            accessInfo.setAccessType("groups");
            for (Group group : turn.getAllowedGroups()) {
                allowedIds.add(group.getId());
            }
        } else if (!turn.getAllowedFaculties().isEmpty()) {
            accessInfo.setAccessType("faculties");
            for (Faculty faculty : turn.getAllowedFaculties()) {
                allowedIds.add(faculty.getId());
            }
        } else {
            accessInfo.setAccessType("for_link");
        }

        accessInfo.setAllowedIds(allowedIds);
        return accessInfo;
    }

    /**
     * Получение очередей пользователя
     * @param login имя пользователя
     * @param params учебная/организационная мои/доступные
     * @return список очередей
     */
    @Transactional
    @Override
    public List<TurnForListDTO> getUserTurns(
            String login,
            Map<String, String> params
    ) {
        // Получаем пользователя по логину
        User user = userService.getUserFromLogin(login);
        // Очищаем устаревшие очереди
        cleanupExpiredTurns();

        // Определяем тип доступа и получаем соответствующие очереди
        String access = params.get("Access");
        List<Object[]> allTurns = fetchTurnsBasedOnAccess(
                user,
                access,
                params.get("Type")
        );

        // Преобразуем результаты в DTO
        return mapTurnsToDTOList(allTurns);
    }

    /**
     * Очищает очереди, у которых дата окончания меньше текущей даты.
     */
    private void cleanupExpiredTurns() {
        Date now = new Date();
        turnRepository.deleteByDateEndIsLessThan(now);
    }

    /**
     * Возвращает список очередей в зависимости от типа доступа пользователя.
     *
     * @param user   Пользователь, для которого запрашиваются очереди.
     * @param access Тип доступа (например, "memberOut" или "memberIn").
     * @param type   Тип очереди.
     * @return Список очередей в виде массива объектов.
     */
    private List<Object[]> fetchTurnsBasedOnAccess(
            User user,
            String access,
            String type
    ) {
        if (Objects.equals(access, "memberOut")) {
            return fetchTurnsForMemberOut(user, type);
        } else if (Objects.equals(access, "memberIn")) {
            return turnRepository.resultsMemberIn(
                    user.getId(),
                    type
            );
        }
        return new ArrayList<>();
    }

    /**
     * Возвращает список очередей для пользователя с доступом "memberOut".
     *
     * @param user Пользователь, для которого запрашиваются очереди.
     * @param type Тип очереди.
     * @return Список очередей в виде массива объектов.
     */
    private List<Object[]> fetchTurnsForMemberOut(
            User user,
            String type
    ) {
        long facultyId = 0L;
        long groupId = 0L;
        Group group = user.getGroup();

        if (group != null) {
            groupId = group.getId();
            if (group.getFaculty() != null) {
                facultyId = group.getFaculty().getId();
            }
        }

        return turnRepository.resultsMemberOut(
                user.getId(),
                groupId,
                facultyId,
                type
        );
    }

    /**
     * Преобразует список массивов объектов в список DTO.
     *
     * @param allTurns Список массивов объектов, содержащих данные очередей.
     * @return Список DTO очередей.
     */
    private List<TurnForListDTO> mapTurnsToDTOList(
            List<Object[]> allTurns
    ) {
        List<TurnForListDTO> turnForList = new ArrayList<>();
        for (Object[] obj : allTurns) {
            Turn turn = (Turn) obj[0];
            String additionalInfo = (String) obj[1];
            turnForList.add(
                    turnForListMapper.turnToTurnForListDTO(
                            turn,
                            additionalInfo
                    )
            );
        }
        return turnForList;
    }

    @Override
    public List<TurnForListDTO> getLinkedTurn(
            String hash,
            String username
    ) {
        User user = userService.getUserFromLogin(username);
        Optional<Turn> t = turnRepository.findTurnByHash(hash);
        if (t.isEmpty()) {
            throw new NotFoundAllTurnsException("no found");
        }
        Turn turn = t.get();
        List<TurnForListDTO> turnList = new ArrayList<>();
        // получение участника и проверка наличия доступа
        Optional<Member> member =
                mbrRepService.getMemberWith(user, turn);
        if (member.isPresent()) {
            AccessMember access = member.get().getAccessMember();
            if (access == AccessMember.BLOCKED) {
                return turnList;
            }
        }

        turnList.add(
                turnForListMapper.turnToTurnForListDTO(
                        turn,
                        null
                )
        );
        return turnList;
    }

}
