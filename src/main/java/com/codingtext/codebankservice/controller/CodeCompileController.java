package com.codingtext.codebankservice.controller;

import com.codingtext.codebankservice.Dto.CodeBank.CodeHistoryDto;
import com.codingtext.codebankservice.Dto.User.UserPoint;
import com.codingtext.codebankservice.Service.CodeHistoryService;
import com.codingtext.codebankservice.Service.CodeUserService;
import com.codingtext.codebankservice.client.UserServiceClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
@Tag(name = "Code Compile API", description = "코딩 테스트 문제제출을 관리하는 API")
@RestController
@RequestMapping("/compile")
@RequiredArgsConstructor
public class CodeCompileController {
    //코드 컴파일,코드 제출할때 사용
    //코테페이지에서 제출하기
    private final CodeHistoryService codeHistoryService;
    private final CodeUserService codeUserService;
    private final UserServiceClient userServiceClient;

    @Operation(summary = "문제 풀이 히스토리 저장 또는 갱신", description = "유저가 문제를 컴파일하면 해당 문제를 히스토리에 저장하거나 이미 기록이있을경 시간 갱신")
    @PostMapping("/{codeId}")
    public ResponseEntity<String> updateHistory(
            @PathVariable Long codeId,
            @RequestHeader("UserId") String userId, // 헤더로 UserId 받기
            @RequestBody CodeHistoryDto historyRequest) {

        try {
            if (userId != null) {
                historyRequest.setCodeId(codeId);
                historyRequest.setUserId(userId);
                codeHistoryService.updateOrAddHistory(historyRequest, userId);
                return ResponseEntity.ok("히스토리 저장 완료");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("유저 또는 히스토리를 찾을수없음");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("히스토리 저장 실패: " + e.getMessage());
        }
    }
    @Operation(summary = "정답제출시 히스토리 생성/갱신 및 점수전달", description = "유저가 문제를 제출하면 해당 문제를 히스토리에 저장하거나 이미 기록이 있을경우 시간및정답여부 갱신 + 유저서비스로 점수전달")
    @PostMapping("/submit/{codeId}")
    public ResponseEntity<String> updateHistoryAndSendPoint(
            @PathVariable Long codeId,
            @RequestHeader("UserId") String userId, // 헤더로 UserId 받기
            @RequestBody CodeHistoryDto historyRequest,
            @RequestParam boolean Correct ) {//정답여부확인,정답=참,오담=거짓

        Optional<Long> historyId = codeHistoryService.getHistoryId(userId, codeId);

        if (historyId.isPresent()) {
            historyRequest.setCodeId(codeId);
            historyRequest.setUserId(userId);
            historyRequest.setIsCorrect(Correct);
            codeHistoryService.updateOrAddHistory(historyRequest, userId);
            int point = codeUserService.calculateUserPoint(userId);
            UserPoint userPoint = new UserPoint(userId,point);
            userServiceClient.updateScore(userPoint);
            //updateScore가 ok인 경우와 아닌경우로 error분기 나누기
            return ResponseEntity.ok("히스토리 저장 완료");
        } else {
            // 히스토리가 없으면 null 반환
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("유저 또는 히스토리를 찾을수없음");
        }
    }

}