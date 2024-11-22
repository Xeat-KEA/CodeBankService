package com.codingtext.codebankservice.Dto.Compile;

import com.codingtext.codebankservice.entity.Code;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CodeWithTestcases {
    private Code code;
    private List<Testcase> testcases;
}

