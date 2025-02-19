package com.eturn.eturn.additionalService.position.impl;

import com.eturn.eturn.additionalService.member.MemberRepositoryService;
import com.eturn.eturn.additionalService.member.MemberStatusService;
import com.eturn.eturn.additionalService.position.PositionCreationService;
import com.eturn.eturn.additionalService.position.PositionTimerService;
import com.eturn.eturn.dto.DetailedPositionDTO;
import com.eturn.eturn.dto.mapper.DetailedPositionMapper;
import com.eturn.eturn.entity.*;
import com.eturn.eturn.enums.AccessTurn;
import com.eturn.eturn.exception.member.NoAccessMemberException;
import com.eturn.eturn.exception.position.NoCreatePosException;
import com.eturn.eturn.notifications.NotificationController;
import com.eturn.eturn.repository.PositionRepository;
import com.eturn.eturn.service.MemberService;
import com.eturn.eturn.service.TurnService;
import com.eturn.eturn.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

import static com.eturn.eturn.enums.AccessMember.BLOCKED;
import static com.eturn.eturn.enums.AccessMember.MEMBER_LINK;
import static com.eturn.eturn.enums.AccessTurn.FOR_LINK;
import static com.eturn.eturn.enums.ChangeMbrAction.ADD_INVITE_STATUS;
import static com.eturn.eturn.enums.InvitedStatus.ACCESS_IN;
import static com.eturn.eturn.enums.MemberListType.MEMBER;

@Service
public class PositionCreationServiceImpl implements PositionCreationService {
    private final PositionRepository positionRepository;
    private final TurnService turnService;
    private final UserService userService;
    private final MemberRepositoryService mbrRepService;
    private final MemberStatusService mbrStatusService;
    private final MemberService memberService;
    private final NotificationController notificationController;
    private final PositionTimerService positionTimerService;
    private final DetailedPositionMapper detailedPositionMapper;

    public PositionCreationServiceImpl(
            PositionRepository positionRepository,
            TurnService turnService,
            UserService userService,
            MemberRepositoryService mbrRepService,
            MemberStatusService mbrStatusService,
            MemberService memberService,
            NotificationController notificationController,
            PositionTimerService positionTimerService,
            DetailedPositionMapper detailedPositionMapper
    ) {
        this.positionRepository = positionRepository;
        this.turnService = turnService;
        this.userService = userService;
        this.mbrRepService = mbrRepService;
        this.mbrStatusService = mbrStatusService;
        this.memberService = memberService;
        this.notificationController = notificationController;
        this.positionTimerService = positionTimerService;
        this.detailedPositionMapper = detailedPositionMapper;
    }

    /**
     * Создание позиции
     * @param login имя пользователя
     * @param hash хэш очереди
     * @return Позиция
     */
    @Override
    @Transactional
    public DetailedPositionDTO createPositionAndSave(String login, String hash) {
        // Получаем очередь и пользователя
        Turn turn = turnService.getTurnFrom(hash);
        User user = userService.getUserFromLogin(login);

        // Получаем участника (Member) или создаем нового, если он не существует
        Member currentMember = getOrCreateMember(user, turn);

        // Проверяем доступ участника
        if (isMemberAllowedToCreatePosition(currentMember)) {
            // Удаляем просроченные элементы очереди
            positionTimerService.deleteOverdueElements(turn);

            // Создаем новую позицию или выбрасываем исключение, если это невозможно
            return createOrHandlePosition(currentMember, turn, user);
        }

        // Если участник заблокирован или не имеет доступа, возвращаем null
        return null;
    }

    /**
     * Создание участника при вставании в очередь
     * @param user пользователь, которого нужно добавить
     * @param turn очередь, в которую нужно добавить
     * @return Созданный участник
     */
    @Override
    public Member createMemberForPosition(User user, Turn turn) {

        // Проверяем тип очереди
        AccessTurn turnEnum = turn.getAccessTurnType();
        if (turnEnum == FOR_LINK) {
            // Отправляем уведомление о новой заявки в очередь
            notificationController.notifyReceiptRequest(turn.getId(), turn.getName());
            return memberService.createMember(user, turn, "MEMBER_LINK", true);
        } else {
            // проверяем, что есть доступ к добавлению
            Set<Group> groups = turn.getAllowedGroups();
            Set<Faculty> faculties = turn.getAllowedFaculties();
            if (groups.contains(user.getGroup()) || faculties.contains(user.getGroup().getFaculty())) {
                return memberService.createMember(user, turn, "MEMBER", false);
            }
            else {
                throw new NoAccessMemberException("You are not this user!");
            }
        }
    }

    /**
     * Получает участника (Member) для пользователя и очереди или создает нового, если он не существует.
     * @param user Пользователь
     * @param turn Очередь
     * @return Существующий или новый участник (Member)
     */
    private Member getOrCreateMember(User user, Turn turn) {
        Optional<Member> member = mbrRepService.getMemberWith(user, turn);
        if (member.isEmpty()) {
            return createMemberForPosition(user, turn);
        } else {
            Member currentMember = member.get();
            handleMemberStatus(currentMember);
            return currentMember;
        }
    }

    /**
     * Обрабатывает статус участника (Member), если он имеет статус MEMBER_LINK.
     * @param member Участник (Member), статус которого нужно обработать
     */
    private void handleMemberStatus(Member member) {
        if (member.getAccessMember() == MEMBER_LINK) {
            switch (member.getInvitedForTurn()) {
                case ACCESS_IN -> mbrStatusService.changeMemberStatusFrom(
                        member.getId(),
                        "MEMBER",
                        Optional.empty(),
                        Optional.empty()
                );
                case ACCESS_OUT -> mbrStatusService.changeMemberStatusFrom(
                        member.getId(),
                        "MEMBER_LINK",
                        Optional.empty(),
                        Optional.of(ADD_INVITE_STATUS)
                );
            }
        }
    }

    /**
     * Проверяет, может ли участник (Member) создавать позицию в очереди.
     * @param member Участник (Member)
     * @return true, если участник не заблокирован и имеет доступ, иначе false
     */
    private boolean isMemberAllowedToCreatePosition(Member member) {
        return member.getAccessMember() != BLOCKED && member.getInvitedForTurn() == ACCESS_IN;
    }

    /**
     * Создает новую позицию или обрабатывает исключение, если создание невозможно.
     * @param member Участник (Member)
     * @param turn Очередь
     * @param user Пользователь
     * @return DTO с информацией о позиции
     * @throws NoCreatePosException Если создание позиции невозможно
     */
    private DetailedPositionDTO createOrHandlePosition(Member member, Turn turn, User user) {
        // Если в очереди уже есть позиции
        if (positionRepository.countAllByTurn(turn) > 0) {
            long countPositions = mbrRepService.getCountMembersWith(turn, MEMBER);
            int permittedCount = calculatePermittedCount(turn, countPositions);

            // Проверяем, может ли пользователь создать новую позицию
            if (isPositionCreationAllowed(turn, user, countPositions, permittedCount)) {
                Position newPosition = createNewPosition(member);
                int differenceForUser = calculatePositionDifference(newPosition, turn, user);
                return detailedPositionMapper.positionMoreInfoToPositionDTO(newPosition, differenceForUser);
            } else {
                // Если создание позиции невозможно, выбрасываем исключение
                throw new NoCreatePosException(String.valueOf(calculateExceptionDifference(turn, user, permittedCount)));
            }
        } else {
            // Если очередь пуста, создаем новую позицию
            Position newPosition = createNewPosition(member);
            return detailedPositionMapper.positionMoreInfoToPositionDTO(newPosition, 0);
        }
    }

    /**
     * Вычисляет допустимое количество позиций в очереди.
     * @param turn Очередь
     * @param countPositions Текущее количество позиций
     * @return Допустимое количество позиций
     */
    private int calculatePermittedCount(Turn turn, long countPositions) {
        int permittedCount = turn.getPositionCount();
        if (permittedCount == -1) {
            throw new NoCreatePosException(String.valueOf(-1));
        } else if (permittedCount == 0) {
            permittedCount = countPositions >= 20 ? (int) (countPositions * 0.8) : 0;
        }
        return permittedCount;
    }

    /**
     * Проверяет, может ли пользователь создать новую позицию.
     * @param turn Очередь
     * @param user Пользователь
     * @param countPositions Текущее количество позиций
     * @param permittedCount Допустимое количество позиций
     * @return true, если создание позиции разрешено, иначе false
     */
    private boolean isPositionCreationAllowed(Turn turn, User user, long countPositions, int permittedCount) {
        Optional<Position> ourPosition = positionRepository.findFirstByUserAndTurnOrderByIdDesc(user, turn);
        if (countPositions < permittedCount && ourPosition.isPresent()) {
            return false;
        }
        return true;
    }

    /**
     * Вычисляет разницу для позиции пользователя.
     * @param newPosition Новая позиция
     * @param turn Очередь
     * @param user Пользователь
     * @return Разница для позиции
     */
    private int calculatePositionDifference(Position newPosition, Turn turn, User user) {
        Optional<Position> ourPosition = positionRepository.findFirstByUserAndTurnOrderByIdDesc(user, turn);
        return ourPosition.isEmpty() ? (int) positionRepository.countIdLeft(newPosition.getId(), turn) : -1;
    }

    /**
     * Вычисляет разницу для исключения, если создание позиции невозможно.
     * @param turn Очередь
     * @param user Пользователь
     * @param permittedCount Допустимое количество позиций
     * @return Разница для исключения
     */
    private int calculateExceptionDifference(Turn turn, User user, int permittedCount) {
        Optional<Position> lastPos = positionRepository.findFirstByTurnOrderByIdDesc(turn);
        Position positionDelete = positionRepository.resultsPositionDelete(turn.getId(), permittedCount);
        if (positionDelete != null) {
            Optional<Position> positionOfUser = positionRepository.findFirstByTurnAndUserAndIdGreaterThanOrderByIdDesc(turn, user, positionDelete.getId());
            if (positionOfUser.isPresent() && lastPos.isPresent()) {
                return permittedCount - positionRepository.countAllByTurnAndIdBetween(turn, positionOfUser.get().getId(), lastPos.get().getId()) + 1;
            }
        }
        return 0;
    }

    /**
     * Создание позиции и заполнение ее параметров
     * @param currentMember участник, который встает в очередь
     * @return созданная позиция
     */
    public Position createNewPosition(Member currentMember) {
        Turn turn = currentMember.getTurn();
        User user = currentMember.getUser();
        Position newPosition = new Position();
        newPosition.setStart(false);
        newPosition.setUser(user);
        newPosition.setTurn(turn);
        newPosition.setDateEnd(null);
        newPosition.setMember(currentMember);
        if (user.getGroup() != null) {
            newPosition.setGroupName(user.getGroup().getNumber());
        }
        if (turn.getPositionCount() != 0) {
            newPosition.setSkipCount(turn.getPositionCount() / 5);
        } else {
            newPosition.setSkipCount((mbrRepService.getCountMembersWith(turn, MEMBER) / 10));
        }
        return positionRepository.save(newPosition);
    }
}
