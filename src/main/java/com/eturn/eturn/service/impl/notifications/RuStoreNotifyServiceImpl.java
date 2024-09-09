package com.eturn.eturn.service.impl.notifications;

import com.eturn.eturn.enums.NotifySendType;
import com.eturn.eturn.notifications.NotificationMessages;
import com.eturn.eturn.service.NotificationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class RuStoreNotifyServiceImpl implements NotificationService {
    private static final Logger logger = LogManager.getLogger(RuStoreNotifyServiceImpl.class);
    @Override
    public void notifyUserOfTurnPositionChange(String tokenFirst, String turnName, int number) {
        String title = NotificationMessages.getPositionTitle(number);
        String body = NotificationMessages.getPositionBody(number, turnName);
        logger.info("Notification body: " + title + " // " + body);
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
