package com.quadrah.sims.repository;

import com.quadrah.sims.model.Notification;
import com.quadrah.sims.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional(readOnly = true) // Add this
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Transactional(readOnly = true) // Add this to read methods
    List<Notification> findByRecipientOrderByCreatedAtDesc(UserAccount recipient);

    @Transactional(readOnly = true)
    List<Notification> findByRecipientAndStatusOrderByCreatedAtDesc(UserAccount recipient, Notification.NotificationStatus status);

    @Transactional(readOnly = true)
    long countByRecipientAndStatus(UserAccount recipient, Notification.NotificationStatus status);

    @Modifying
    @Transactional // Add explicit transaction for modifying queries
    @Query("UPDATE Notification n SET n.status = 'READ', n.readAt = CURRENT_TIMESTAMP WHERE n.id = :id AND n.recipient.id = :userId")
    int markAsRead(@Param("id") Long id, @Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.status = 'READ', n.readAt = CURRENT_TIMESTAMP WHERE n.recipient.id = :userId AND n.status = 'UNREAD'")
    int markAllAsRead(@Param("userId") Long userId);

    @Transactional(readOnly = true)
    List<Notification> findByTypeAndStatusOrderByCreatedAtDesc(Notification.NotificationType type, Notification.NotificationStatus status);

    @Transactional(readOnly = true)
    List<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);

    @Transactional(readOnly = true)
    List<Notification> findAllByOrderByCreatedAtDesc();
}