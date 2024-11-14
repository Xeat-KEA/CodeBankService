package com.codingtext.codebankservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "code_history")
public class CodeHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long codeHistoryId;

    @ManyToOne
    @JoinColumn(name = "code_id", nullable = false)
    private Code code;

    //String으로 바꿀것
    @Column(nullable = false)
    private String userId;

    @Column(nullable = false, length = 3000)
    private String writtenCode;

    @Column(nullable = false)
    private Boolean isCorrect;

    @Column(nullable = false)
    private Boolean isCreatedByAI;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime compiledAt;

    public CodeHistory(Code code, String userId, String writtenCode, Boolean isCorrect, Boolean isCreatedByAI, LocalDateTime createdAt, LocalDateTime compiledAt) {
        this.code = code;
        this.userId = userId;
        this.writtenCode = writtenCode;
        this.isCorrect = isCorrect;
        this.isCreatedByAI = isCreatedByAI;
        this.createdAt = createdAt;
        this.compiledAt = compiledAt;
    }


}
