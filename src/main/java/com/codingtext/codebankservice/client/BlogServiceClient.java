package com.codingtext.codebankservice.client;

import com.codingtext.codebankservice.Dto.Blog.RegisterRequestDto;
import feign.Response;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "blog-service")
public interface BlogServiceClient {
    @PostMapping("/blog/notice/code")
    RegisterRequestDto saveCodeNotice(@RequestBody RegisterRequestDto registerRequestDto);

}
