package com.eturn.eturn.notifications;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class NotificationController {

    private static final Logger logger = LogManager.getLogger(NotificationController.class);
    @Value("${eturn.rabbitmq.topic}")
    private String TOPIC;
    @Value("${eturn.rabbitmq.exchange}")
    private String EXCHANGE;
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
        rabbitTemplate.convertAndSend(EXCHANGE, TOPIC, notification);
    }

    public void notifyTurnCreated(long groupId, String turnName) {
        Notification notification = new Notification();
        notification.setType(1);
        notification.setTurnName(turnName);
        notification.setGroupId(groupId);
        logger.info("Notification sent to broker");
        rabbitTemplate.convertAndSend(EXCHANGE, TOPIC, notification);
    }

    public void notifyReceiptRequest(long turnId, String turnName) {
        Notification notification = new Notification();
        notification.setType(2);
        notification.setTurnId(turnId);
        notification.setTurnName(turnName);
        logger.info("Notification sent to broker");
        rabbitTemplate.convertAndSend(EXCHANGE, TOPIC, notification);
    }
}
