package com.codingtext.codebankservice.Dto;

import com.codingtext.codebankservice.Entity.Code;
import com.codingtext.codebankservice.Entity.CodeHistory;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Optional;

@Getter
@Setter
public class CodeHistoryDto {
    private Long codeHistoryId;
    private Long codeId;
    private Long userId;
    private String writtenCode;
    private Boolean isCorrect;
    private LocalDateTime createdAt;
    private LocalDateTime compiledAt;


    public static CodeHistoryDto ToDto(CodeHistory history) {
        CodeHistoryDto historyDto = new CodeHistoryDto();
        historyDto.setCodeHistoryId(history.getCodeHistoryId());
        historyDto.setCodeId(history.getCode().getCodeId());
        historyDto.setUserId(history.getUserId());
        historyDto.setWrittenCode(history.getWrittenCode());
        historyDto.setIsCorrect(history.getIsCorrect());
        historyDto.setCreatedAt(history.getCreatedAt());
        historyDto.setCompiledAt(history.getCompiledAt());
        return historyDto;
    }
}
