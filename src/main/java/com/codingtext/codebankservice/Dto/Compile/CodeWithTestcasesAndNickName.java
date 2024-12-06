package com.codingtext.codebankservice.Dto.Compile;

import com.codingtext.codebankservice.entity.Code;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CodeWithTestcasesAndNickName {
    private String nickName;
    private Code code;
    private List<Testcase> testcases;
}
