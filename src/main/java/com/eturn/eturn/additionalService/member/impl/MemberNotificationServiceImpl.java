package com.eturn.eturn.additionalService.member.impl;

import com.eturn.eturn.additionalService.member.MemberNotificationService;
import com.eturn.eturn.entity.Turn;
import com.eturn.eturn.notifications.NotificationController;
import org.springframework.stereotype.Service;

@Service
public class MemberNotificationServiceImpl implements MemberNotificationService {

    private final NotificationController notificationController;

    public MemberNotificationServiceImpl(
            NotificationController notificationController
    ) {
        this.notificationController = notificationController;
    }

    /**
     * Отправляет уведомление о запросе на участие в очереди.
     */
    public void notifyReceiptRequest(Turn turn) {
        notificationController.notifyReceiptRequest(turn.getId(), turn.getName());
    }
}
