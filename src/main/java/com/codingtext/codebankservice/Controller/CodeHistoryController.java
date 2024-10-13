package com.codingtext.codebankservice.Controller;

import com.codingtext.codebankservice.Dto.CodeHistoryDto;
import com.codingtext.codebankservice.Service.CodeHistoryService;
import com.codingtext.codebankservice.Service.CodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
            @RequestParam(defaultValue = "0") int page,  // 기본값: 0페이지
            @RequestParam(defaultValue = "10") int size  // 기본값: 페이지당 10개
    ) {
        List<CodeHistoryDto> historyDtos = codeHistoryService.getUserHistory(userId, page, size);
        return ResponseEntity.ok(historyDtos);}

}
