package com.codingtext.codebankservice.kafka;

import java.time.LocalDateTime;

public class RollbackEvent {
    private String userId;
    private LocalDateTime timestamp;

    // 생성자
    public RollbackEvent(String userId, LocalDateTime timestamp) {
        this.userId = userId;
        this.timestamp = timestamp;
    }

    // Getter 및 Setter
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}

