package com.codingtext.codebankservice.client;

import com.codingtext.codebankservice.Dto.CodeDto;
import com.codingtext.codebankservice.Dto.CodeWithTestcases;
import com.codingtext.codebankservice.Dto.Testcase;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "LLM-service", url = "llm-service")
public interface LLMServiceClient {

    //문제 생성 요청
//    @PostMapping("/llm/code-generating")
//    ResponseEntity<LLMResponseDTO.CodeGenerateResponse> codeGenerator(@RequestBody LLMRequestDTO.codeGeneratingInfo request);

}