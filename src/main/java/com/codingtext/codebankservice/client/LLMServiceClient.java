package com.codingtext.codebankservice.client;

import com.codingtext.codebankservice.Dto.LLM.LLMRequestDTO;
import com.codingtext.codebankservice.Dto.LLM.LLMResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "code-llm-service")
public interface LLMServiceClient {
    //문제생성 요청
    @PostMapping("/llm/code-generating")
    ResponseEntity<LLMResponseDTO.CodeGenerateClientResponse> codeGenerator(@RequestBody LLMRequestDTO.codeGeneratingInfo request);

}