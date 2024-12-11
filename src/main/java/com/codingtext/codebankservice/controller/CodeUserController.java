package com.codingtext.codebankservice.controller;

import com.codingtext.codebankservice.Dto.User.UserPoint;
import com.codingtext.codebankservice.Service.CodeUserService;
import com.codingtext.codebankservice.repository.CodeHistoryRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Code user API", description = "유저서비스 정보전달")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class CodeUserController {
    //문제제출하기->히스토리에서 점수계산하기->User서비스가 호출하면 보내주기
    //어떤 순간에 계산해서 언제 보내줘야하지?
    //userId와 점수만 리턴
    private final CodeUserService codeUserService;
    private final CodeHistoryRepository codeHistoryRepository;

    @Operation(summary = "유저 점수 계산-테스트용", description = "유저아이디 기반으로 조회 유저아이디+점수 반환")
    @GetMapping("/point")
    public ResponseEntity<UserPoint> getPoint(@RequestHeader("UserId") String userId) {
        int point = codeUserService.calculateUserPoint(userId);
        int count = codeUserService.calculateUserCount(userId);
        UserPoint userPoint = new UserPoint(userId,point,count);
        return ResponseEntity.ok(userPoint);
    }
    @Operation(summary = "특정유저 히스토리 전부삭제", description = "유저아이디 기반으로 히스토리를 삭제한다-탈퇴용")
    @DeleteMapping ("/delete")
    public ResponseEntity<String> DeleteHistory(@RequestHeader("UserId") String userId) {
        codeHistoryRepository.deleteByUserId(userId);
        return ResponseEntity.ok("해당 유저아이디의 히스토리를 삭제했습니다");
    }


//    @Operation(summary = "유저아이디확인용 나중에 삭제할것", description = "유저아이디 반환")
//    @GetMapping("/id")
//    public String getUserId(@RequestHeader("UserId") String userId) {
//        System.out.println("userId="+userId);
//        return userId;
//    }

}
