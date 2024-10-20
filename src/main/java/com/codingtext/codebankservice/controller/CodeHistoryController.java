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
    public ResponseEntity<List<CodeHistoryDto>> getUserHistory(@PathVariable Long userId,@PageableDefault(page = 0, size = 10) Pageable pageable) {
        List<CodeHistoryDto> historyDtos = codeHistoryService.getUserHistory(userId, pageable.getPageNumber(), pageable.getPageSize());
        return ResponseEntity.ok(historyDtos);}

}
