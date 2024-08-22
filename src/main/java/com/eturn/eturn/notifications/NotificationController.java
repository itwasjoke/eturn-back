package com.eturn.eturn.notifications;

import com.eturn.eturn.entity.Group;
import com.eturn.eturn.entity.User;
import com.eturn.eturn.enums.ApplicationType;
import com.eturn.eturn.service.NotificationService;
import com.eturn.eturn.service.impl.notifications.AndroidNotifyServiceImpl;
import com.eturn.eturn.service.impl.notifications.RuStoreNotifyServiceImpl;
import com.eturn.eturn.service.impl.notifications.IOSNotifyServiceImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class NotificationController {

    private static Logger logger = LogManager.getLogger(NotificationController.class);

    private static final String TOPIC_EASY = "easyNotifications";
    private static final String TOPIC_HARD = "hardNotifications";

    private final RabbitTemplate rabbitTemplate;
    private final AndroidNotifyServiceImpl androidNotifyService;
    private final IOSNotifyServiceImpl iOSNotifyService;
    private final RuStoreNotifyServiceImpl ruStoreNotifyService;

    public NotificationController(
            RabbitTemplate rabbitTemplate,
            AndroidNotifyServiceImpl androidNotifyService,
            IOSNotifyServiceImpl iOSNotifyService,
            RuStoreNotifyServiceImpl ruStoreNotifyService
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.androidNotifyService = androidNotifyService;
        this.iOSNotifyService = iOSNotifyService;
        this.ruStoreNotifyService = ruStoreNotifyService;
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

    public void notifyUserOfTurnPositionChange(Long id) {
        Notification notification = new Notification();
        notification.setTurnId(id);
        notification.setType(0);
        List<Long> list = new ArrayList<>();
        list.add(1L);
        notification.setGroupList(list);
        rabbitTemplate.convertAndSend("easy-notifications", "easy-notifications", notification);
    }

    @RabbitListener(queues = "easy-queue", messageConverter = "jackson2JsonMessageConverter")
    public void receiveMessage(Notification notification) {
        logger.info("MESSAGE FROM TURN: " + notification.type);
    }

    public void notifyTurnCreated(Long id, List<Group> list) {

    }

    public void notifyReceiptRequest(List<User> users) {

    }
}
