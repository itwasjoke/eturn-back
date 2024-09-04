package com.eturn.eturn.repository;

import com.eturn.eturn.entity.Notification;
import com.eturn.eturn.enums.NotifyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification,Long> {
    Optional<Notification> findNotificationByUserIdAndType(Long id, NotifyType type);
    void deleteAllByCreatedBefore(Date date);
    boolean existsAllByCreatedBefore(Date date);
}
