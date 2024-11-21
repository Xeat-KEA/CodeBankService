package com.codingtext.codebankservice.client;

import com.codingtext.codebankservice.Dto.CodeIdWithTestcases;
//import com.codingtext.codebankservice.Dto.CodeWithId;
import com.codingtext.codebankservice.Dto.Testcase;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "code-compile-service", url = "code-compile-service")
public interface CompileServiceClient {

    //문제 생성시 testcase저장요청
    //+
    //문제 정식 승인시 변경사항있을때 업데이트 요청
    @PostMapping("/code")
    void saveCode(@RequestBody CodeIdWithTestcases codeIdWithTestcases);

    @DeleteMapping("/code/submit/{id}")
    void removeCode(@PathVariable Integer id);

    @GetMapping("/code/{id}")
    BaseResponse<CodeIdWithTestcases> findCode(@PathVariable Integer id);




}