package com.eturn.eturn.service.impl.notifications;

import com.eturn.eturn.enums.NotifySendType;
import com.eturn.eturn.notifications.NotificationMessages;
import com.eturn.eturn.service.NotificationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

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
        String title = NotificationMessages.getPositionTitle(number);
        String body = NotificationMessages.getPositionBody(number, turnName);
        logger.info("Notification body: " + title + " // " + body);
//        String payload = APNS.newPayload().alertTitle(title).alertBody(body).build();
//        apnsService.push(tokenFirst, payload);
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
