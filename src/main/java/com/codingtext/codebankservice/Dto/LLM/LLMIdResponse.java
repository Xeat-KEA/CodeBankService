package com.codingtext.codebankservice.Dto.LLM;

import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LLMIdResponse {
    private Long codeId;
    private Long historyId;
}
