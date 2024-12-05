package com.codingtext.codebankservice.client;

import com.codingtext.codebankservice.Dto.User.UserInfoDto;
import com.codingtext.codebankservice.Dto.User.UserPoint;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "user-service")
public interface UserServiceClient {
    @PutMapping("/score")
    ResponseEntity<?> updateScore(@RequestBody UserPoint userPoint);

    @GetMapping("/users/userInfo")
    ResponseEntity<UserInfoDto> getUserInfo(String userId);

}
