package com.codingtext.codebankservice.Dto;

import java.time.LocalDateTime;

public class CodeHistoryDto {
    private Long codeHistoryId;
    private Long codeId;
    private Long userId;
    private String writtenCode;
    private Boolean isCorrect;
    private LocalDateTime createdAt;
    private LocalDateTime compiledAt;

    // Getters and Setters
    public Long getCodeHistoryId() {
        return codeHistoryId;
    }

    public void setCodeHistoryId(Long codeHistoryId) {
        this.codeHistoryId = codeHistoryId;
    }

    public Long getCodeId() {
        return codeId;
    }

    public void setCodeId(Long codeId) {
        this.codeId = codeId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getWrittenCode() {
        return writtenCode;
    }

    public void setWrittenCode(String writtenCode) {
        this.writtenCode = writtenCode;
    }

    public Boolean getIsCorrect() {
        return isCorrect;
    }

    public void setIsCorrect(Boolean isCorrect) {
        this.isCorrect = isCorrect;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getCompiledAt() {
        return compiledAt;
    }

    public void setCompiledAt(LocalDateTime compiledAt) {
        this.compiledAt = compiledAt;
    }
}
