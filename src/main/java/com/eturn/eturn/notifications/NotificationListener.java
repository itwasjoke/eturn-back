package com.eturn.eturn.notifications;

import com.eturn.eturn.entity.User;
import com.eturn.eturn.enums.ApplicationType;
import com.eturn.eturn.service.MemberService;
import com.eturn.eturn.service.NotificationService;
import com.eturn.eturn.service.PositionService;
import com.eturn.eturn.service.UserService;
import com.eturn.eturn.service.impl.notifications.AndroidNotifyServiceImpl;
import com.eturn.eturn.service.impl.notifications.IOSNotifyServiceImpl;
import com.eturn.eturn.service.impl.notifications.RuStoreNotifyServiceImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NotificationListener {
    private static final Logger logger = LogManager.getLogger(NotificationListener.class);
    private static final String TURN_EASY = "easy-queue";
    private final AndroidNotifyServiceImpl androidNotifyService;
    private final IOSNotifyServiceImpl iOSNotifyService;
    private final RuStoreNotifyServiceImpl ruStoreNotifyService;
    private final PositionService positionService;
    private final UserService userService;
    private final MemberService memberService;

    public NotificationListener(
            AndroidNotifyServiceImpl androidNotifyService,
            IOSNotifyServiceImpl iOSNotifyService,
            RuStoreNotifyServiceImpl ruStoreNotifyService,
            PositionService positionService,
            UserService userService, MemberService memberService) {
        this.androidNotifyService = androidNotifyService;
        this.iOSNotifyService = iOSNotifyService;
        this.ruStoreNotifyService = ruStoreNotifyService;
        this.positionService = positionService;
        this.userService = userService;
        this.memberService = memberService;
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
        switch (notification.type) {
            case 0 -> sendNotificationFor3Positions(notification);
            case 1 -> sendNotificationForGroup(notification);
            case 2 -> sendNotificationForModerators(notification);
            default -> {
            }
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

    private void sendNotificationForGroup(Notification notification){
        long groupId = notification.groupId;
        List<User> userList = userService.getGroupUsers(groupId);
        for (User u: userList) {
            if (typeExists(u)) {
                NotificationService notificationService = getCurrentService(u.getApplicationType());
                notificationService.notifyTurnCreated(u.getTokenNotification(), notification.turnName);
            }
        }
    }
    private void sendNotificationForModerators(Notification notification){
        long turnId = notification.turnId;
        List<User> userList = memberService.getModeratorsOfTurn(turnId);
        for (User u: userList) {
            if (typeExists(u)) {
                NotificationService notificationService = getCurrentService(u.getApplicationType());
                notificationService.notifyReceiptRequest(u.getTokenNotification(), notification.turnName);
            }
        }
    }
}
