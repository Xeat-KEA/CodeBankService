package com.codingtext.codebankservice.Dto;

import com.codingtext.codebankservice.Dto.Compile.Testcase;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminResponse {
    private Long codeId;
    private String title;
    private String codeContent;
    private List<Testcase> testcases;
}