package com.eturn.eturn.notifications;

import com.eturn.eturn.entity.Notification;
import com.eturn.eturn.entity.User;
import com.eturn.eturn.enums.ApplicationType;
import com.eturn.eturn.enums.NotifySendType;
import com.eturn.eturn.enums.NotifyType;
import com.eturn.eturn.repository.NotificationRepository;
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

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Component
public class NotificationListener {
    private static final Logger logger = LogManager.getLogger(NotificationListener.class);
    private static final String TURN_EASY = "eturn-queue";

    private final NotificationRepository notificationRepository;
    private final AndroidNotifyServiceImpl androidNotifyService;
    private final IOSNotifyServiceImpl iOSNotifyService;
    private final RuStoreNotifyServiceImpl ruStoreNotifyService;
    private final PositionService positionService;
    private final UserService userService;
    private final MemberService memberService;

    public NotificationListener(
            NotificationRepository notificationRepository, AndroidNotifyServiceImpl androidNotifyService,
            IOSNotifyServiceImpl iOSNotifyService,
            RuStoreNotifyServiceImpl ruStoreNotifyService,
            PositionService positionService,
            UserService userService, MemberService memberService) {
        this.notificationRepository = notificationRepository;
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
    public void receiveMessage(NotificationDTO notificationDTO) {
        logger.info("Notification delivered to listener");
        switch (notificationDTO.type) {
            case 0 -> sendNotificationFor3Positions(notificationDTO);
            case 1 -> sendNotificationForGroup(notificationDTO);
            case 2 -> sendNotificationForModerators(notificationDTO);
            default -> {
            }
        }
    }

    private NotifySendType checkNotification(NotificationDTO notificationDTO, User user) {
        NotifyType notifyType = getNotifyType(notificationDTO);
        if (notifyType == null) {
            return NotifySendType.NO_ACCESS;
        }
        deleteNotifications();
        Optional<Notification> existingNotification = notificationRepository.findNotificationByUserIdAndType(user.getId(), notifyType);
        if (existingNotification.isPresent()) {
            return handleExistingNotification(existingNotification.get());
        } else {
            return handleNewNotification(user, notifyType);
        }
    }

    private void deleteNotifications(){
        Date currentDate = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        calendar.add(Calendar.MINUTE, -30);
        Date date30MinutesAgo = calendar.getTime();
        notificationRepository.deleteAllByCreatedLessThan(date30MinutesAgo);
    }

    private NotifyType getNotifyType(NotificationDTO notificationDTO) {
        return switch (notificationDTO.type) {
            case 0 -> NotifyType.POSITION;
            case 1 -> NotifyType.GROUPS;
            case 2 -> NotifyType.INVITED;
            default -> null;
        };
    }

    private NotifySendType handleExistingNotification(Notification notification) {
        if (notification.isMany()) {
            if (isNotificationExpired(notification.getCreated())) {
                updateNotification(notification, false, new Date());
                return NotifySendType.ONE;
            } else {
                return NotifySendType.NO_ACCESS;
            }
        } else {
            if (isNotificationExpired(notification.getCreated())) {
                updateNotification(notification, false, new Date());
                return NotifySendType.ONE;
            } else {
                updateNotification(notification, true, notification.getCreated());
                return NotifySendType.MANY;
            }
        }
    }

    private NotifySendType handleNewNotification(User user, NotifyType notifyType) {
        Notification notification = new Notification();
        notification.setMany(false);
        notification.setType(notifyType);
        notification.setCreated(new Date());
        notification.setUserId(user.getId());
        notificationRepository.save(notification);
        return NotifySendType.ONE;
    }

    private boolean isNotificationExpired(Date createdDate) {
        Date currentDate = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        calendar.add(Calendar.MINUTE, -15);
        Date date15MinutesAgo = calendar.getTime();
        return createdDate.before(date15MinutesAgo);
    }

    private void updateNotification(Notification notification, boolean many, Date date) {
        notification.setCreated(date);
        notification.setMany(many);
        notificationRepository.save(notification);
    }

    private void sendNotificationFor3Positions(NotificationDTO notificationDTO){
        PositionsNotificationDTO info = positionService.getPositionsForNotify(notificationDTO.turnId);
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

    private void sendNotificationForGroup(NotificationDTO notificationDTO){
        long groupId = notificationDTO.groupId;
        List<User> userList = userService.getGroupUsers(groupId);
        for (User u: userList) {
            if (typeExists(u)) {
                NotifySendType sendType = checkNotification(notificationDTO, u);
                if (sendType != NotifySendType.NO_ACCESS) {
                    logger.info(sendType);
                    NotificationService notificationService = getCurrentService(u.getApplicationType());
                    notificationService.notifyTurnCreated(u.getTokenNotification(), notificationDTO.turnName, sendType);
                }
            }
        }
    }
    private void sendNotificationForModerators(NotificationDTO notificationDTO){
        long turnId = notificationDTO.turnId;
        List<User> userList = memberService.getModeratorsOfTurn(turnId);
        for (User u: userList) {
            if (typeExists(u)) {
                NotifySendType sendType = checkNotification(notificationDTO, u);
                if (sendType != NotifySendType.NO_ACCESS) {
                    NotificationService notificationService = getCurrentService(u.getApplicationType());
                    notificationService.notifyReceiptRequest(u.getTokenNotification(), notificationDTO.turnName, sendType);
                }
            }
        }
    }
}
