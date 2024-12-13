package com.codingtext.codebankservice.Dto.CodeBank;

import lombok.*;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class CodeContentWithHistoryAndHistoryIdAndIsCorrect {
    private String code_Content;
    private String codeHistory_writtenCode;
    private Long historyId;
    private boolean isCorrect;
}
