package com.quadrah.sims.dto;

import com.quadrah.sims.model.Notification;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Notification data transfer object")
public class NotificationDTO {

    @Schema(description = "Notification ID", example = "1")
    private Long id;

    @Schema(description = "Notification title", example = "🚨 Emergency Visit Alert")
    private String title;

    @Schema(description = "Notification message", example = "Emergency visit for John Smith (Grade: 10). Reason: Severe allergic reaction")
    private String message;

    @Schema(description = "Notification type", example = "EMERGENCY_VISIT")
    private String type;

    @Schema(description = "Notification status", example = "UNREAD")
    private String status;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Read timestamp (null if unread)")
    private LocalDateTime readAt;

    @Schema(description = "Related entity type", example = "StudentVisit")
    private String relatedEntityType;

    @Schema(description = "Related entity ID", example = "123")
    private Long relatedEntityId;

    @Schema(description = "Recipient user information")
    private UserDTO recipient;

    // Constructor from Entity
    public NotificationDTO(Notification notification) {
        this.id = notification.getId();
        this.title = notification.getTitle();
        this.message = notification.getMessage();
        this.type = notification.getType().name();
        this.status = notification.getStatus().name();
        this.createdAt = notification.getCreatedAt();
        this.readAt = notification.getReadAt();
        this.relatedEntityType = notification.getRelatedEntityType();
        this.relatedEntityId = notification.getRelatedEntityId();

        // Convert recipient to DTO to avoid proxy issues
        if (notification.getRecipient() != null) {
            this.recipient = new UserDTO(notification.getRecipient());
        }
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getReadAt() { return readAt; }
    public void setReadAt(LocalDateTime readAt) { this.readAt = readAt; }
    public String getRelatedEntityType() { return relatedEntityType; }
    public void setRelatedEntityType(String relatedEntityType) { this.relatedEntityType = relatedEntityType; }
    public Long getRelatedEntityId() { return relatedEntityId; }
    public void setRelatedEntityId(Long relatedEntityId) { this.relatedEntityId = relatedEntityId; }
    public UserDTO getRecipient() { return recipient; }
    public void setRecipient(UserDTO recipient) { this.recipient = recipient; }
}