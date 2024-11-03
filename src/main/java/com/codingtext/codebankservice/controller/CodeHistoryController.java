package com.codingtext.codebankservice.controller;

import com.codingtext.codebankservice.Dto.CodeHistoryDto;
import com.codingtext.codebankservice.Service.CodeHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Tag(name = "Code History API", description = "사용자의 문제 풀이 히스토리를 관리하는 API")
@RestController
@RequestMapping("/code/history")
public class CodeHistoryController {
    @Autowired
    private CodeHistoryService codeHistoryService;

    // 특정 유저의 히스토리 조회
    @Operation(summary = "특정 유저의 히스토리 조회", description = "특정 유저의 문제 풀이 히스토리를 페이징하여 조회")
    @GetMapping("/{userId}")
    public ResponseEntity<List<CodeHistoryDto>> getUserHistory(
            @PathVariable Long userId,
            @PageableDefault(page = 0, size = 10) Pageable pageable) {

        List<CodeHistoryDto> historyDtos = codeHistoryService.getUserHistory(
                userId, pageable.getPageNumber(), pageable.getPageSize());
        return ResponseEntity.ok(historyDtos);
    }

    // 유저가 컴파일 시 해당 문제를 히스토리에 저장 또는 갱신
    @Operation(summary = "문제 풀이 히스토리 저장 또는 갱신", description = "유저가 문제를 컴파일하면 해당 문제를 히스토리에 저장하거나 이미 기록이있을경 시간 갱신")
    @PostMapping("/compile/{codeId}")
    public ResponseEntity<String> updateHistory(
            @PathVariable Long codeId,
            @RequestHeader("UserId") Long userId, // 헤더로 UserId 받기
            @RequestBody CodeHistoryDto historyRequest) {

        historyRequest.setCodeId(codeId); // codeId를 요청 바디에 설정
        historyRequest.setUserId(userId); // userId를 요청 바디에 설정
        codeHistoryService.updateOrAddHistory(historyRequest);
        return ResponseEntity.ok("히스토리 저장 완료");
    }
}
