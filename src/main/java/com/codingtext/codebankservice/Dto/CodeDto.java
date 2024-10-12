package com.codingtext.codebankservice.Dto;

import java.time.LocalDateTime;
public class CodeDto {
    private Long codeId;
    private String title;
    private String content;
    private String difficulty;
    private String algorithm;
    private String registerstatus;
    private LocalDateTime createdAt;

    public Long getCodeId() {
        return codeId;
    }

    public void setCodeId(Long codeId) {
        this.codeId = codeId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getRegisterStatus() {
        return registerstatus;
    }

    public void setRegisterStatus(String registerstatus) {
        this.registerstatus = registerstatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }


}
