package com.eturn.eturn.service.impl.notifications;

import java.util.List;
import com.eturn.eturn.service.NotificationService;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class AndroidNotifyServiceImpl implements NotificationService {
    private static final Logger logger = LogManager.getLogger(AndroidNotifyServiceImpl.class);
    @Override
    public void notifyUserOfTurnPositionChange(String tokenFirst, String turnName, int number) {
        String title = number == 0 ? "Вы следующий" : "Ваша позиция уже скоро";
        String body = "В очереди \"" + turnName + "\" ";
        body = number == 0 ? body + "настал ваш черёд. Нажмите, чтобы войти" : "перед вами " + number + "человек. Будьте наготове!";
        logger.info("Notification body: " + title + " // " + body);
//        Message message = Message.builder()
//                .setToken(tokenFirst)
//                .setNotification(Notification.builder()
//                        .setTitle(title)
//                        .setBody(body)
//                        .build()
//                )
//                .build();
//        try {
//            FirebaseMessaging.getInstance().send(message);
//        } catch (FirebaseMessagingException e) {
//            logger.error("Error with sending notification: " + e.getMessage());
//        }
    }

    @Override
    public void notifyTurnCreated(List<String> tokens, String turnName) {

    }

    @Override
    public void notifyReceiptRequest(List<String> tokens) {

    }
}
