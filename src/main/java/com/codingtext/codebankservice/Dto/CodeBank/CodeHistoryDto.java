package com.codingtext.codebankservice.Dto.CodeBank;

import com.codingtext.codebankservice.entity.CodeHistory;
import com.codingtext.codebankservice.entity.Difficulty;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeHistoryDto {
    private Long codeHistoryId;
    private Long codeId;
    private String userId;
    private String writtenCode;
    private Boolean isCorrect;
    private Boolean isCreatedByAI;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime compiledAt;
    //문제이름 히스토리 표시용
    private String codeTitle;



    public static CodeHistoryDto ToDto(CodeHistory history) {
        CodeHistoryDto historyDto = new CodeHistoryDto();
        historyDto.setCodeHistoryId(history.getCodeHistoryId());
        historyDto.setCodeId(history.getCode().getCodeId());
        historyDto.setUserId(history.getUserId());
        historyDto.setWrittenCode(history.getWrittenCode());
        historyDto.setIsCorrect(history.getIsCorrect());
        historyDto.setIsCreatedByAI(history.getIsCreatedByAI());
        historyDto.setCreatedAt(history.getCreatedAt());
        historyDto.setCompiledAt(history.getCompiledAt());
        historyDto.setCodeTitle(history.getCode().getTitle());
        System.out.println("CodeHistory ID in DTO: " + history.getCodeHistoryId());
        return historyDto;
    }
}
