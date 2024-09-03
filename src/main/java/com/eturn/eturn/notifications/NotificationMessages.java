package com.eturn.eturn.notifications;

import com.eturn.eturn.enums.NotifySendType;

public class NotificationMessages {
    public static String getTurnCreatedTitle(NotifySendType notifySendType) {
        switch (notifySendType) {
            case MANY -> {
                return "Вам доступно несколько очередей";
            }
            case ONE -> {
                return "Вам доступна новая очередь";
            }
            case NO_ACCESS -> {
                return "";
            }
        }
        return "Доступны новые очереди";
    }

    public static String getTurnCreatedBody(NotifySendType notifySendType, String turnName) {
        switch (notifySendType) {
            case MANY -> {
                return "Созданы очереди, в которые вы можете встать прямо сейчас";
            }
            case ONE -> {
                return "Создана новая очередь под названием \"" + turnName + "\"";
            }
            case NO_ACCESS -> {
                return "";
            }
        }
        return "Созданы очереди, в которые вы можете встать прямо сейчас";
    }

    public static String getReceiptTitle(NotifySendType notifySendType) {
        switch (notifySendType) {
            case MANY -> {
                return "Новые заявки";
            }
            case ONE -> {
                return "Новая заявка";
            }
            case NO_ACCESS -> {
                return "";
            }
        }
        return "";
    }

    public static String getReceiptBody(String turnName){
        return "Обработайте заявки на вступление в очереди \"" + turnName + "\"";
    }

    public static String getPositionTitle(Integer number){
        return number == 0 ? "Вы следующий" : "Ваша позиция уже скоро";
    }

    public static String getPositionBody(Integer number, String turnName) {
        String body = "В очереди \"" + turnName + "\" ";
        return number == 0 ? body + "настал ваш черёд. Нажмите, чтобы войти" : "перед вами " + number + "человек. Будьте наготове!";
    }
}
