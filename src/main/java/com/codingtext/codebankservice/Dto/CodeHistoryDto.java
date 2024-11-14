package com.codingtext.codebankservice.Dto;

import com.codingtext.codebankservice.entity.Code;
import com.codingtext.codebankservice.entity.CodeHistory;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Optional;

@Getter
@Setter
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
        return historyDto;
    }
}
