package com.codingtext.codebankservice.Dto.CodeBank;

import com.codingtext.codebankservice.entity.Difficulty;
import lombok.*;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class CodeContentWithHistoryAndHistoryIdAndIsCorrectAndDifficulty {
    private String code_Content;
    private String codeHistory_writtenCode;
    private Long historyId;
    private boolean isCorrect;
    private Difficulty difficulty;
}
