package com.eturn.eturn.service.impl.notifications;

import com.eturn.eturn.service.NotificationService;
import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IOSNotifyServiceImpl implements NotificationService {
//    private final ApnsService apnsService;
//
//    public IOSNotifyServiceImpl(ApnsService apnsService) {
//        this.apnsService = apnsService;
//    }
    @Override
    public void notifyUserOfTurnPositionChange(String tokenFirst, String turnName, int number) {
        String title = number == 0 ? "Вы следующий" : "Ваша позиция уже скоро";
        String body = "В очереди \"" + turnName + "\" ";
        body = number == 0 ? body + "Вы следующий. Вперёд!" : "перед вами "+ number + "человек. Будьте наготове!";
        String payload = APNS.newPayload().alertTitle(title).alertBody(body).build();
//        apnsService.push(tokenFirst, payload);
    }

    @Override
    public void notifyTurnCreated(List<String> tokens, String turnName) {

    }

    @Override
    public void notifyReceiptRequest(List<String> tokens) {

    }
}
