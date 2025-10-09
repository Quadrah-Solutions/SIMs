package com.quadrah.sims.controller;

import com.quadrah.sims.dto.NotificationDTO;
import com.quadrah.sims.service.NotificationService;
import com.quadrah.sims.service.UserAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    private final NotificationService notificationService;
    private final UserAccountService userAccountService; // Keep UserAccountService

    public NotificationController(NotificationService notificationService,
                                  UserAccountService userAccountService) {
        this.notificationService = notificationService;
        this.userAccountService = userAccountService;
    }

    @GetMapping
    public ResponseEntity<?> getUserNotifications() {
        try {
            logger.info("Fetching notifications for current user");

            // Use UserAccountService to get current user
            var currentUser = userAccountService.getCurrentUser();
            logger.info("Current user: {} (ID: {})", currentUser.getUsername(), currentUser.getId());

            List<NotificationDTO> notifications = notificationService.getUserNotifications(currentUser.getId());
            logger.info("Found {} notifications", notifications.size());

            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            logger.error("Error fetching notifications", e);
            return ResponseEntity.status(500).body(createErrorResponse("Failed to fetch notifications: " + e.getMessage()));
        }
    }

    @GetMapping("/unread")
    public ResponseEntity<?> getUnreadNotifications() {
        try {
            var currentUser = userAccountService.getCurrentUser();
            List<NotificationDTO> notifications = notificationService.getUnreadUserNotifications(currentUser.getId());
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            logger.error("Error fetching unread notifications", e);
            return ResponseEntity.status(500).body(createErrorResponse("Failed to fetch unread notifications: " + e.getMessage()));
        }
    }

    @GetMapping("/unread-count")
    public ResponseEntity<?> getUnreadNotificationCount() {
        try {
            var currentUser = userAccountService.getCurrentUser();
            long count = notificationService.getUnreadNotificationCount(currentUser.getId());
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            logger.error("Error fetching unread count", e);
            return ResponseEntity.status(500).body(createErrorResponse("Failed to fetch unread count: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        try {
            var currentUser = userAccountService.getCurrentUser();
            notificationService.markAsRead(id, currentUser.getId());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error marking notification as read", e);
            return ResponseEntity.status(500).body(createErrorResponse("Failed to mark notification as read: " + e.getMessage()));
        }
    }

    @PostMapping("/read-all")
    public ResponseEntity<?> markAllAsRead() {
        try {
            var currentUser = userAccountService.getCurrentUser();
            notificationService.markAllAsRead(currentUser.getId());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error marking all notifications as read", e);
            return ResponseEntity.status(500).body(createErrorResponse("Failed to mark all notifications as read: " + e.getMessage()));
        }
    }

    private Map<String, String> createErrorResponse(String message) {
        return Map.of("error", message);
    }
}