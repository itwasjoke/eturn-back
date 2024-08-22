package com.eturn.eturn.notifications;

import com.eturn.eturn.entity.User;
import com.eturn.eturn.enums.ApplicationType;
import com.eturn.eturn.service.NotificationService;
import com.eturn.eturn.service.PositionService;
import com.eturn.eturn.service.impl.notifications.AndroidNotifyServiceImpl;
import com.eturn.eturn.service.impl.notifications.IOSNotifyServiceImpl;
import com.eturn.eturn.service.impl.notifications.RuStoreNotifyServiceImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.weaver.ast.Not;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationListener {
    private static final Logger logger = LogManager.getLogger(NotificationListener.class);
    private static final String TURN_EASY = "easy-queue";
    private final AndroidNotifyServiceImpl androidNotifyService;
    private final IOSNotifyServiceImpl iOSNotifyService;
    private final RuStoreNotifyServiceImpl ruStoreNotifyService;
    private final PositionService positionService;

    public NotificationListener(
            AndroidNotifyServiceImpl androidNotifyService,
            IOSNotifyServiceImpl iOSNotifyService,
            RuStoreNotifyServiceImpl ruStoreNotifyService,
            PositionService positionService
    ) {
        this.androidNotifyService = androidNotifyService;
        this.iOSNotifyService = iOSNotifyService;
        this.ruStoreNotifyService = ruStoreNotifyService;
        this.positionService = positionService;
    }

    private NotificationService getCurrentService(ApplicationType appType) {
        switch (appType) {
            case ANDROID -> {
                return androidNotifyService;
            }
            case IOS -> {
                return iOSNotifyService;
            }
            case RUSTORE -> {
                return ruStoreNotifyService;
            }
        }
        return null;
    }
    private boolean typeExists(User user) {
        return user.getApplicationType() != null;
    }
    @RabbitListener(queues = TURN_EASY, messageConverter = "jackson2JsonMessageConverter")
    public void receiveMessage(Notification notification) {
        logger.info("Notification delivered to listener");
        switch (notification.type){
            case 0:
                sendNotificationFor3Positions(notification);
            case 1:
                logger.info("There will be a notification of new members here");
            default:
        }
    }

    private void sendNotificationFor3Positions(Notification notification){
        PositionsNotificationDTO info = positionService.getPositionsForNotify(notification.turnId);
        if (info.userList() != null && info.turnName() != null) {
            int num = 0;
            for (User u : info.userList()) {
                if (typeExists(u)) {
                    NotificationService notificationService = getCurrentService(u.getApplicationType());
                    notificationService.notifyUserOfTurnPositionChange(u.getTokenNotification(), info.turnName(), num);
                }
                num += 5;
            }
        }
    }
}
