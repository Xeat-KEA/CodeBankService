package com.codingtext.codebankservice.controller;

import com.codingtext.codebankservice.Dto.UserPoint;
import com.codingtext.codebankservice.Service.CodeService;
import com.codingtext.codebankservice.Service.CodeUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Code user API", description = "유저서비스 정보전달")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class CodeUser {
    //문제제출하기->히스토리에서 점수계산하기->User서비스가 호출하면 보내주기
    //어떤 순간에 계산해서 언제 보내줘야하지?
    //userId와 점수만 리턴
    private final CodeUserService codeUserService;

    @Operation(summary = "특정유저의 점수를 계산해서 리턴하는 기능", description = "유저아이디 기반으로 조회 유저아이디+점수 반환")
    @GetMapping("/point")
    public ResponseEntity<UserPoint> getPoint(@RequestHeader("UserId") String userId) {
        int point = codeUserService.calculateUserPoint(userId);
        UserPoint userPoint = new UserPoint(userId,point);
        return ResponseEntity.ok(userPoint);
    }
    @Operation(summary = "유저아이디확인용 나중에 삭제할것", description = "유저아이디 반환")
    @GetMapping("/id")
    public String getUserId(@RequestHeader("UserId") String userId) {

        return userId;
    }

}
