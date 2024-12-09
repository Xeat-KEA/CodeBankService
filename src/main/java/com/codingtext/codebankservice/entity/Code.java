package com.codingtext.codebankservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "code")
public class Code {
    //enum타입 분리
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long codeId;

    @Column(nullable = false, length = 30)
    private String title;

    @Column(nullable = false)
    @Lob
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
    public Code(String title, String content, Difficulty difficulty, Algorithm algorithm, LocalDateTime createdAt, RegisterStatus registerStatus) {
        this.title = title;
        this.content = content;
        this.difficulty = difficulty;
        this.algorithm = algorithm;
        this.createdAt = createdAt;
        this.registerStatus = registerStatus;
    }

}

