package com.codingtext.codebankservice.Dto.Compile;

import com.codingtext.codebankservice.entity.Algorithm;
import com.codingtext.codebankservice.entity.Code;
import com.codingtext.codebankservice.entity.Difficulty;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@Getter
@Setter
@Builder
public class CodeWithTestcasesForEdit {
    Long codeId;
    String title;
    String content;
    Difficulty difficulty;
    Algorithm algorithm;
    private List<Testcase> testcases;
}
