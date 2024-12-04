package com.codingtext.codebankservice.Dto.Compile;

import com.codingtext.codebankservice.entity.Code;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@Getter
@Setter
@Builder
public class CodeWithTestcases {
    private Code code;
    private List<Testcase> testcases;
}

