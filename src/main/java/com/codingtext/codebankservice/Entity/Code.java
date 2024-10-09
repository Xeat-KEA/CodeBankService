package com.codingtext.codebankservice.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "code")
public class Code {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long codeId;

    @Column(nullable = false, length = 30)
    private String title;

    @Column(nullable = false, length = 300)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty difficulty;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Algorithm algorithm;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RegisterStatus registerStatus;

    // Getters and Setters...
}

enum Difficulty {
    EASY, MEDIUM, HARD
}

enum Algorithm {
    DP, GRAPH
}

enum RegisterStatus {
    CREATED, REQUESTED, REGISTERED
}
