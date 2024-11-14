package com.codingtext.codebankservice.client;

import com.codingtext.codebankservice.Dto.CodeDto;
import com.codingtext.codebankservice.Dto.CodeWithTestcases;
import com.codingtext.codebankservice.Dto.Testcase;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "LLM-service", url = "llm-service")
public interface LLMServiceClient {

    //문제 정식 승인시 변경사항있을때 업데이트 요청
    @PostMapping("/LLM/create")
    void createProblem(@RequestBody CodeDto codeDto);

    //codeId기반으로 testcase가져오기
    @GetMapping("/LLM/create")
    List<CodeWithTestcases> getTestcasesByCodeId(@RequestBody CodeWithTestcases codeWithTestcases);

}