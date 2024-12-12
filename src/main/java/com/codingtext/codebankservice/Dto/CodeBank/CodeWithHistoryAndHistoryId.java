package com.codingtext.codebankservice.Dto.CodeBank;

import com.codingtext.codebankservice.entity.Code;
import com.codingtext.codebankservice.entity.CodeHistory;
import lombok.*;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class CodeWithHistoryAndHistoryId {
    private String code_Content;
    private String codeHistory_writtenCode;
    private Long historyId;
}
