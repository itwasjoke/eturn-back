package com.eturn.eturn.service.impl.notifications;

import com.eturn.eturn.enums.NotifySendType;
import com.eturn.eturn.notifications.NotificationMessages;
import com.eturn.eturn.service.NotificationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.weaver.ast.Not;
import org.springframework.stereotype.Service;

@Service
public class AndroidNotifyServiceImpl implements NotificationService {
    private static final Logger logger = LogManager.getLogger(AndroidNotifyServiceImpl.class);
    @Override
    public void notifyUserOfTurnPositionChange(String tokenFirst, String turnName, int number) {
        String title = NotificationMessages.getPositionTitle(number);
        String body = NotificationMessages.getPositionBody(number, turnName);
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
    public void notifyTurnCreated(String token, String turnName, NotifySendType sendType) {
        String title = NotificationMessages.getTurnCreatedTitle(sendType);
        String body = NotificationMessages.getTurnCreatedBody(sendType, turnName);
        logger.info("Notification body: " + title + " // " + body);
    }

    @Override
    public void notifyReceiptRequest(String token, String turnName, NotifySendType sendType) {
        String title = NotificationMessages.getReceiptTitle(sendType);
        String body = NotificationMessages.getReceiptBody(turnName);
        logger.info("Notification body: " + title + " // " + body);
    }
}
