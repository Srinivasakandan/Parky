package com.smartparking.entity;

import java.time.LocalDateTime;

public class AuditLog {

    private Long id;
    private Long userId;
    private String userEmail;
    private String action;
    private String description;
    private LocalDateTime timestamp = LocalDateTime.now();

    public AuditLog() {}

    public AuditLog(Long userId, String userEmail, String action, String description) {
        this.userId = userId;
        this.userEmail = userEmail;
        this.action = action;
        this.description = description;
        this.timestamp = LocalDateTime.now();
    }

    public AuditLog(Long id, Long userId, String userEmail, String action, String description, LocalDateTime timestamp) {
        this.id = id;
        this.userId = userId;
        this.userEmail = userEmail;
        this.action = action;
        this.description = description;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
