package com.codingtext.codebankservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "code-compile-service", url = "http://172.16.211.60:18008/code-compile-service")
public interface CompileServiceClient {

    @DeleteMapping("/compile/delete/{codeId}")
    void deleteCompileData(@PathVariable("codeId") Long codeId);
}