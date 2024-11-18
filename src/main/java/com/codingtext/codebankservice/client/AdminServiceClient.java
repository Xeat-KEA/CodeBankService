package com.codingtext.codebankservice.client;

import com.codingtext.codebankservice.Dto.AdminResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "admin-service", url = "admin-service")
public interface AdminServiceClient {

    @GetMapping("/admin/code/{codeId}")
    ResponseEntity<AdminResponse> getCodeWithTestcasesFromAdmin(@PathVariable Long codeId);
}