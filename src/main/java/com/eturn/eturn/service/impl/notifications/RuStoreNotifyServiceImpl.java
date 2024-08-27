package com.eturn.eturn.service.impl.notifications;

import com.eturn.eturn.service.NotificationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class RuStoreNotifyServiceImpl implements NotificationService {
    private static final Logger logger = LogManager.getLogger(RuStoreNotifyServiceImpl.class);
    @Override
    public void notifyUserOfTurnPositionChange(String tokenFirst, String turnName, int number) {
        String title = number == 0 ? "Вы следующий" : "Ваша позиция уже скоро";
        String body = "В очереди \"" + turnName + "\" ";
        body = number == 0 ? body + "настал ваш черёд. Нажмите, чтобы войти" : "перед вами " + number + "человек. Будьте наготове!";
        logger.info("Notification body: " + title + " // " + body);
    }

    @Override
    public void notifyTurnCreated(String token, String turnName) {
        String title = "Вам доступна новая очередь";
        String body = "Создана новая очередь под названием \"" + turnName + "\"";
        logger.info("Notification body: " + title + " // " + body);
    }

    @Override
    public void notifyReceiptRequest(String token, String turnName) {
        String title = "Новая заявка";
        String body = "Обработайте заявки на вступление в очереди \"" + turnName + "\"";
        logger.info("Notification body: " + title + " // " + body);
    }
}
