package com.quadrah.sims.service;

import com.quadrah.sims.dto.NotificationDTO;
import com.quadrah.sims.model.*;
import com.quadrah.sims.repository.NotificationRepository;
import com.quadrah.sims.repository.StudentRepository;
import com.quadrah.sims.repository.UserAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserAccountRepository userAccountRepository;
    private final StudentRepository studentRepository;
    private final SimpMessagingTemplate messagingTemplate;

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    public NotificationService(NotificationRepository notificationRepository,
                               UserAccountRepository userAccountRepository,
                               StudentRepository studentRepository,
                               SimpMessagingTemplate messagingTemplate) {
        this.notificationRepository = notificationRepository;
        this.userAccountRepository = userAccountRepository;
        this.studentRepository = studentRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public void notifyEmergencyVisit(StudentVisit visit) {
        // Ensure we have a fully loaded student by fetching it fresh
        Student student = getFullyLoadedStudent(visit.getStudent().getId());

        String title = "🚨 Emergency Visit Alert";
        String message = String.format(
                "Emergency visit for %s %s (Grade: %s). Reason: %s",
                student.getFirstName(),
                student.getLastName(),
                student.getGradeLevel(),
                visit.getReason()
        );

        // Notify all nurses and admins
        List<UserAccount> recipients = userAccountRepository.findByRoleIn(
                List.of(UserAccount.UserRole.NURSE, UserAccount.UserRole.ADMIN)
        );

        for (UserAccount recipient : recipients) {
            Notification notification = createNotification(
                    title, message, Notification.NotificationType.EMERGENCY_VISIT,
                    recipient, "StudentVisit", visit.getId()
            );

            sendRealTimeNotification(recipient, notification);
        }

        logger.info("EMERGENCY: Visit #{} for student {} {}",
                visit.getId(), student.getFirstName(), student.getLastName());
    }

    public void notifyDispositionChange(StudentVisit visit) {
        Student student = getFullyLoadedStudent(visit.getStudent().getId());

        String title = "📋 Disposition Update";
        String message = String.format(
                "Student %s %s disposition changed to: %s",
                student.getFirstName(),
                student.getLastName(),
                visit.getDisposition()
        );

        // Notify the nurse who created the visit and admins
        List<UserAccount> recipients = userAccountRepository.findByRole(UserAccount.UserRole.ADMIN);
        recipients.add(visit.getNurse());

        for (UserAccount recipient : recipients) {
            Notification notification = createNotification(
                    title, message, Notification.NotificationType.DISPOSITION_CHANGE,
                    recipient, "StudentVisit", visit.getId()
            );

            sendRealTimeNotification(recipient, notification);
        }

        logger.info("Disposition updated for Visit #{}: {}", visit.getId(), visit.getDisposition());
    }

    // Helper method to get fully loaded student
    private Student getFullyLoadedStudent(Long studentId) {
        // You'll need to inject StudentRepository
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + studentId));
    }

    public void notifyLowStock(MedicationInventory medication) {
        String title = "⚠️ Low Stock Alert";
        String message = String.format(
                "%s is running low. Current stock: %d, Minimum required: %d",
                medication.getMedicationName(),
                medication.getCurrentStock(),
                medication.getMinimumStock()
        );

        // Notify nurses and admins
        List<UserAccount> recipients = userAccountRepository.findByRoleIn(
                List.of(UserAccount.UserRole.NURSE, UserAccount.UserRole.ADMIN)
        );

        for (UserAccount recipient : recipients) {
            Notification notification = createNotification(
                    title, message, Notification.NotificationType.LOW_STOCK,
                    recipient, "MedicationInventory", medication.getId()
            );

            sendRealTimeNotification(recipient, notification);
        }

        logger.info("LOW STOCK: {} (Current: {}, Minimum: {})",
                medication.getMedicationName(), medication.getCurrentStock(), medication.getMinimumStock());
    }

    public void notifyStudentCheckup(Student student, UserAccount nurse) {
        String title = "📝 Student Checkup Reminder";
        String message = String.format(
                "Regular checkup for %s %s (Grade: %s) is due",
                student.getFirstName(), student.getLastName(), student.getGradeLevel()
        );

        Notification notification = createNotification(
                title, message, Notification.NotificationType.STUDENT_CHECKUP,
                nurse, "Student", student.getId()
        );

        sendRealTimeNotification(nurse, notification);
    }

    private Notification createNotification(String title, String message,
                                            Notification.NotificationType type,
                                            UserAccount recipient,
                                            String relatedEntityType, Long relatedEntityId) {
        Notification notification = new Notification(title, message, type, recipient);
        notification.setRelatedEntityType(relatedEntityType);
        notification.setRelatedEntityId(relatedEntityId);

        return notificationRepository.save(notification);
    }

    // Real-time WebSocket notification
    private void sendRealTimeNotification(UserAccount recipient, Notification notification) {
        try {
            String destination = "/topic/notifications/" + recipient.getId();
            NotificationDTO notificationDTO = new NotificationDTO(notification);
            messagingTemplate.convertAndSend(destination, notificationDTO);
            logger.debug("Sent real-time notification to user {}: {}", recipient.getId(), notificationDTO.getTitle());
        } catch (Exception e) {
            logger.error("Failed to send real-time notification to user {}", recipient.getId(), e);
        }
    }

    // Service methods for managing notifications - UPDATED TO RETURN DTOS
    public List<NotificationDTO> getUserNotifications(Long userId) {
        try {
            UserAccount user = userAccountRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

            List<Notification> notifications = notificationRepository.findByRecipientOrderByCreatedAtDesc(user);

            // Convert to DTOs to avoid proxy serialization issues
            return notifications.stream()
                    .map(NotificationDTO::new)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            logger.error("Error getting notifications for user {}", userId, e);
            throw new RuntimeException("Failed to get user notifications", e);
        }
    }

    public List<NotificationDTO> getUnreadUserNotifications(Long userId) {
        try {
            UserAccount user = userAccountRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

            List<Notification> notifications = notificationRepository.findByRecipientAndStatusOrderByCreatedAtDesc(
                    user, Notification.NotificationStatus.UNREAD
            );

            // Convert to DTOs
            return notifications.stream()
                    .map(NotificationDTO::new)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            logger.error("Error getting unread notifications for user {}", userId, e);
            throw new RuntimeException("Failed to get unread notifications", e);
        }
    }

    public long getUnreadNotificationCount(Long userId) {
        try {
            UserAccount user = userAccountRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
            return notificationRepository.countByRecipientAndStatus(
                    user, Notification.NotificationStatus.UNREAD
            );
        } catch (Exception e) {
            logger.error("Error getting unread count for user {}", userId, e);
            throw new RuntimeException("Failed to get unread count", e);
        }
    }

    public void markAsRead(Long notificationId, Long userId) {
        try {
            int updated = notificationRepository.markAsRead(notificationId, userId);
            if (updated == 0) {
                logger.warn("No notification found with ID {} for user {}", notificationId, userId);
            } else {
                logger.debug("Marked notification {} as read for user {}", notificationId, userId);
            }
        } catch (Exception e) {
            logger.error("Error marking notification {} as read for user {}", notificationId, userId, e);
            throw new RuntimeException("Failed to mark notification as read", e);
        }
    }

    public void markAllAsRead(Long userId) {
        try {
            int updated = notificationRepository.markAllAsRead(userId);
            logger.debug("Marked {} notifications as read for user {}", updated, userId);
        } catch (Exception e) {
            logger.error("Error marking all notifications as read for user {}", userId, e);
            throw new RuntimeException("Failed to mark all notifications as read", e);
        }
    }
}