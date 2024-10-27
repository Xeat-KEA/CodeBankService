package com.codingtext.codebankservice.controller;

import com.codingtext.codebankservice.Dto.CodeHistoryDto;
import com.codingtext.codebankservice.Service.CodeHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/code/history")
public class CodeHistoryController {
    @Autowired
    private CodeHistoryService codeHistoryService;

    // 특정 유저의 히스토리 조회
    @GetMapping("/{userId}")
    public ResponseEntity<List<CodeHistoryDto>> getUserHistory(
            @PathVariable Long userId,
            @PageableDefault(page = 0, size = 10) Pageable pageable) {

        List<CodeHistoryDto> historyDtos = codeHistoryService.getUserHistory(
                userId, pageable.getPageNumber(), pageable.getPageSize());
        return ResponseEntity.ok(historyDtos);
    }

    // 유저가 컴파일 시 해당 문제를 히스토리에 저장 또는 갱신
    @PostMapping("/{codeId}/compile")
    public ResponseEntity<String> updateHistory(
            @PathVariable Long codeId,
            @RequestBody CodeHistoryDto historyRequest) {

        historyRequest.setCodeId(codeId); // codeId를 요청 바디에 설정
        codeHistoryService.updateOrAddHistory(historyRequest);
        return ResponseEntity.ok("히스토리 저장 완료");
    }
}
