package com.codingtext.codebankservice.Dto.Compile;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CodeWithTestcasesAndNickName {
    private String nickName;
    private CodeWithTestcases codeWithTestcases;
}
