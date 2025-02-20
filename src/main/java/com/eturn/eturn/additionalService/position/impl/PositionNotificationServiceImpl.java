package com.eturn.eturn.additionalService.position.impl;

import com.eturn.eturn.additionalService.position.PositionNotificationService;
import com.eturn.eturn.entity.Position;
import com.eturn.eturn.entity.User;
import com.eturn.eturn.notifications.PositionsNotificationDTO;
import com.eturn.eturn.repository.PositionRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PositionNotificationServiceImpl implements PositionNotificationService {

    private static final Logger logger = LogManager.getLogger(PositionNotificationServiceImpl.class);
    private final PositionRepository positionRepository;

    public PositionNotificationServiceImpl(PositionRepository positionRepository) {
        this.positionRepository = positionRepository;
    }

    /**
     * Получение пользователей для отправки уведомлений
     * в случае с движением очереди
     * Создание предупреждений для пользователей
     * @param turnId ID очереди
     * @return Список пользователей и название очереди
     */
    @Override
    public PositionsNotificationDTO getPositionsForNotify(Long turnId) {

        // получение последних 10 позиций из очереди
        Pageable paging = PageRequest.of(0, 10);
        Page<Position> page = positionRepository.findAllByTurn_IdOrderByIdAsc(turnId,paging);
        List<Position> list = page.toList();
        logger.info("The turn is now up to " + list.size() + " values");

        // получение 2-3 пользователей и создание списка
        List<User> users = new ArrayList<>();
        if (!list.isEmpty()) {
            Position p1 = list.get(0);
            users.add(p1.getUser());
            if (list.size() > 4) users.add(list.get(4).getUser());
            if (list.size() > 9) users.add(list.get(9).getUser());
            return new PositionsNotificationDTO(users, p1.getTurn().getName());
        } else {
            logger.warn("No notifications will send for "+ turnId +" turn");
            return new PositionsNotificationDTO(null, null);
        }
    }
}
