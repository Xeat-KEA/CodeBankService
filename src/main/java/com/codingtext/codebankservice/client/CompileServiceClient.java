package com.codingtext.codebankservice.client;

import com.codingtext.codebankservice.Dto.Testcase;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "code-compile-service", url = "code-compile-service")
public interface CompileServiceClient {

    //문제삭제할시 해당 Id를 참조하는 testcase삭제요청
    @DeleteMapping("/compile/submit/{Id}")
    void deleteCompileData(@PathVariable("Id") Long Id);

    //문제 정식 승인시 변경사항있을때 업데이트 요청
    @PostMapping("/compile/send/{Id}")
    void sendTestcases(@RequestParam Long codeId, @RequestBody List<Testcase> testcases);

    //codeId기반으로 testcase가져오기
    @GetMapping("/testcase/{codeId}")
    List<Testcase> getTestcasesByCodeId(@PathVariable Long codeId);

}