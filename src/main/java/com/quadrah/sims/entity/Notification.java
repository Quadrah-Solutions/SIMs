package com.quadrah.sims.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification extends BaseEntity {
    private String title;
    private String message;

    @ManyToOne
    private User sender;

    @ManyToOne
    private User recipient;

    private LocalDateTime readAt;

    @Builder.Default
    private boolean isRead = false;

    private LocalDateTime expiryDate;
}