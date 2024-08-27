package com.eturn.eturn.notifications;

import com.eturn.eturn.entity.Group;
import com.eturn.eturn.entity.User;
import com.eturn.eturn.service.PositionService;
import com.eturn.eturn.service.impl.notifications.AndroidNotifyServiceImpl;
import com.eturn.eturn.service.impl.notifications.RuStoreNotifyServiceImpl;
import com.eturn.eturn.service.impl.notifications.IOSNotifyServiceImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NotificationController {

    private static final Logger logger = LogManager.getLogger(NotificationController.class);
    private static final String TOPIC_EASY = "easy-notifications";
    private final RabbitTemplate rabbitTemplate;


    public NotificationController(
             RabbitTemplate rabbitTemplate
    ) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void notifyUserOfTurnPositionChange(Long turnId) {
        Notification notification = new Notification();
        notification.setTurnId(turnId);
        notification.setType(0);
        logger.info("Notification sent to broker");
        rabbitTemplate.convertAndSend(TOPIC_EASY, TOPIC_EASY, notification);
    }

    public void notifyTurnCreated(long groupId, String turnName) {
        Notification notification = new Notification();
        notification.setType(1);
        notification.setTurnName(turnName);
        notification.setGroupId(groupId);
        logger.info("Notification sent to broker");
        rabbitTemplate.convertAndSend(TOPIC_EASY, TOPIC_EASY, notification);
    }

    public void notifyReceiptRequest(long turnId, String turnName) {
        Notification notification = new Notification();
        notification.setType(2);
        notification.setTurnId(turnId);
        notification.setTurnName(turnName);
        logger.info("Notification sent to broker");
        rabbitTemplate.convertAndSend(TOPIC_EASY, TOPIC_EASY, notification);
    }
}
