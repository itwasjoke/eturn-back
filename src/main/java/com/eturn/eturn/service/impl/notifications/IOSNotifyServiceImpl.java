package com.eturn.eturn.service.impl.notifications;

import com.eturn.eturn.notifications.NotificationController;
import com.eturn.eturn.service.NotificationService;
import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IOSNotifyServiceImpl implements NotificationService {
    private static final Logger logger = LogManager.getLogger(IOSNotifyServiceImpl.class);
//    private final ApnsService apnsService;
//
//    public IOSNotifyServiceImpl(ApnsService apnsService) {
//        this.apnsService = apnsService;
//    }
    @Override
    public void notifyUserOfTurnPositionChange(String tokenFirst, String turnName, int number) {
        String title = number == 0 ? "Вы следующий" : "Ваша позиция уже скоро";
        String body = "В очереди \"" + turnName + "\" ";
        body = number == 0 ? body + "настал ваш черёд. Нажмите, чтобы войти" : "перед вами " + number + "человек. Будьте наготове!";
        logger.info("Notification body: " + title + " // " + body);
//        String payload = APNS.newPayload().alertTitle(title).alertBody(body).build();
//        apnsService.push(tokenFirst, payload);
    }

    @Override
    public void notifyTurnCreated(List<String> tokens, String turnName) {

    }

    @Override
    public void notifyReceiptRequest(List<String> tokens) {

    }
}
