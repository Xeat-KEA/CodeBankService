package com.codingtext.codebankservice.client;

import com.codingtext.codebankservice.Dto.User.UserPoint;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(name = "user-service")
public interface UserServiceClient {
    @PutMapping("/score")
    ResponseEntity<?> updateScore(@RequestBody UserPoint userPoint);

}
