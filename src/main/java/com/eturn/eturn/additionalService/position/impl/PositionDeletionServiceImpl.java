package com.eturn.eturn.additionalService.position.impl;

import com.eturn.eturn.additionalService.member.MemberRepositoryService;
import com.eturn.eturn.additionalService.member.MemberStatusService;
import com.eturn.eturn.additionalService.position.PositionDeletionService;
import com.eturn.eturn.entity.Member;
import com.eturn.eturn.entity.Position;
import com.eturn.eturn.entity.Turn;
import com.eturn.eturn.entity.User;
import com.eturn.eturn.enums.AccessMember;
import com.eturn.eturn.exception.member.NoAccessMemberException;
import com.eturn.eturn.exception.position.NoAccessPosException;
import com.eturn.eturn.exception.position.NotFoundPosException;
import com.eturn.eturn.notifications.NotificationController;
import com.eturn.eturn.repository.PositionRepository;
import com.eturn.eturn.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

import static com.eturn.eturn.enums.AccessMember.MODERATOR;
import static com.eturn.eturn.enums.AccessTurn.FOR_LINK;

@Service
public class PositionDeletionServiceImpl implements PositionDeletionService {
    private final PositionRepository positionRepository;
    private final UserService userService;
    private final MemberRepositoryService mbrRepService;
    private final NotificationController notificationController;
    private final MemberStatusService mbrStatusService;

    public PositionDeletionServiceImpl(PositionRepository positionRepository, UserService userService, MemberRepositoryService mbrRepService, NotificationController notificationController, MemberStatusService mbrStatusService) {
        this.positionRepository = positionRepository;
        this.userService = userService;
        this.mbrRepService = mbrRepService;
        this.notificationController = notificationController;
        this.mbrStatusService = mbrStatusService;
    }


    /**
     * Удаляет позицию из очереди, если пользователь имеет на это право.
     * Также обновляет следующую позицию и обрабатывает статус участника.
     * @param id ID позиции для удаления
     * @param username Имя пользователя, выполняющего удаление
     * @throws NoAccessMemberException Если пользователь не является участником очереди
     * @throws NoAccessPosException Если пользователь не имеет доступа для удаления позиции
     * @throws NotFoundPosException Если позиция не найдена
     */
    @Override
    @Transactional
    public void delete(Long id, String username) {
        // Получаем пользователя и позицию
        User user = userService.getUserFromLogin(username);
        Position position = getPositionById(id);

        // Проверяем, является ли пользователь участником очереди
        Member member = getMember(user, position.getTurn());

        // Проверяем доступ пользователя к удалению позиции
        checkDeleteAccess(member, position);

        // Удаляем позицию
        positionRepository.delete(position);

        // Обновляем следующую позицию и обрабатываем статус участника
        updateNextPositionAndMemberStatus(position, member, user);
    }

    /**
     * Получает позицию по ID или выбрасывает исключение, если позиция не найдена.
     *
     * @param id ID позиции
     * @return Позиция
     * @throws NotFoundPosException Если позиция не найдена
     */
    private Position getPositionById(Long id) {
        return positionRepository.findById(id)
                .orElseThrow(() -> new NotFoundPosException("No positions found"));
    }

    /**
     * Получает участника (Member) для пользователя и очереди или выбрасывает исключение, если участник не найден.
     * @param user Пользователь
     * @param turn Очередь
     * @return Участник (Member)
     * @throws NoAccessMemberException Если пользователь не является участником очереди
     */
    private Member getMember(User user, Turn turn) {
        return mbrRepService.getMemberWith(user, turn)
                .orElseThrow(() -> new NoAccessMemberException("You are not a member"));
    }

    /**
     * Проверяет, имеет ли пользователь право на удаление позиции.
     * @param member Участник (Member)
     * @param position Позиция
     * @throws NoAccessPosException Если пользователь не имеет доступа для удаления позиции
     */
    private void checkDeleteAccess(Member member, Position position) {
        AccessMember access = member.getAccessMember();
        if (!(access == AccessMember.MEMBER && position.getUser().getId().equals(member.getUser().getId())
                && access != AccessMember.CREATOR
                && access != MODERATOR)) {
            throw new NoAccessPosException("No access");
        }
    }

    /**
     * Обновляет следующую позицию и обрабатывает статус участника.
     * @param position Удаленная позиция
     * @param member Участник (Member)
     * @param user Пользователь
     */
    private void updateNextPositionAndMemberStatus(Position position, Member member, User user) {
        // Находим следующую позицию в очереди
        positionRepository.findFirstByTurnOrderByIdAsc(position.getTurn()).ifPresent(nextPosition -> {
            // Уведомляем пользователей об изменении позиции
            notificationController.notifyUserOfTurnPositionChange(nextPosition.getTurn().getId());

            // Устанавливаем новое время окончания для следующей позиции
            Date newEndDate = calculateNewEndDate(nextPosition.getTurn().getTimer());
            nextPosition.setDateEnd(newEndDate);
            positionRepository.save(nextPosition);

            // Обрабатываем статус участника, если это необходимо
            handleMemberStatusAfterDeletion(position, member, user);
        });
    }

    /**
     * Вычисляет новое время окончания для позиции на основе таймера.
     * @param timer Таймер очереди в минутах
     * @return Новое время окончания
     */
    private Date calculateNewEndDate(int timer) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.MINUTE, timer);
        return calendar.getTime();
    }

    /**
     * Обрабатывает статус участника после удаления позиции.
     * @param position Удаленная позиция
     * @param member Участник (Member)
     * @param user Пользователь
     */
    private void handleMemberStatusAfterDeletion(Position position, Member member, User user) {
        Optional<Position> userPosition = positionRepository.findFirstByUserAndTurnOrderByIdAsc(user, position.getTurn());
        if (userPosition.isEmpty()) {
            if (member.getAccessMember() == AccessMember.MEMBER && position.getTurn().getAccessTurnType() == FOR_LINK) {
                mbrStatusService.changeMemberStatusFrom(member.getId(), "MEMBER_LINK", Optional.empty(), Optional.empty());
            } else if (member.getAccessMember() == AccessMember.MEMBER) {
                mbrRepService.deleteMemberWith(position.getTurn(), user);
            }
        }
    }

    /**
     * удаление позиций пользователя в очереди
     * @param turn очередь
     * @param user пользователь
     */
    @Override
    public void deleteAllByTurnAndUser(Turn turn, User user) {
        positionRepository.deleteAllByTurnAndUser(turn, user);
    }

}
