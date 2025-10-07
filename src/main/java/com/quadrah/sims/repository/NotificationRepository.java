package com.quadrah.sims.repository;

import com.quadrah.sims.model.Notification;
import com.quadrah.sims.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // FIXED: Added the missing method
    List<Notification> findByRecipientOrderByCreatedAtDesc(UserAccount recipient);

    List<Notification> findByRecipientAndStatusOrderByCreatedAtDesc(UserAccount recipient, Notification.NotificationStatus status);

    long countByRecipientAndStatus(UserAccount recipient, Notification.NotificationStatus status);

    @Modifying
    @Query("UPDATE Notification n SET n.status = 'READ', n.readAt = CURRENT_TIMESTAMP WHERE n.id = :id AND n.recipient.id = :userId")
    int markAsRead(@Param("id") Long id, @Param("userId") Long userId);

    @Modifying
    @Query("UPDATE Notification n SET n.status = 'READ', n.readAt = CURRENT_TIMESTAMP WHERE n.recipient.id = :userId AND n.status = 'UNREAD'")
    int markAllAsRead(@Param("userId") Long userId);

    List<Notification> findByTypeAndStatusOrderByCreatedAtDesc(Notification.NotificationType type, Notification.NotificationStatus status);

    // ADDED: Method to find all notifications for a user
    List<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);

    // ADDED: Method for debugging
    List<Notification> findAllByOrderByCreatedAtDesc();
}