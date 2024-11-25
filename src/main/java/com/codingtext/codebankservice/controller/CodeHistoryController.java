package com.codingtext.codebankservice.controller;

import com.codingtext.codebankservice.Dto.CodeBank.CodeHistoryDto;
import com.codingtext.codebankservice.Service.CodeHistoryService;
import com.codingtext.codebankservice.entity.Code;
import com.codingtext.codebankservice.repository.CodeHistoryRepository;
import com.codingtext.codebankservice.repository.CodeRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Tag(name = "Code History API", description = "사용자의 문제 풀이 히스토리를 관리하는 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/code/history")
public class CodeHistoryController {
    private final CodeRepository codeRepository;
    private final CodeHistoryRepository codeHistoryRepository;
    private final CodeHistoryService codeHistoryService;

    //코드히스토리 아이디를 조회하는 기능
    @Operation(summary = "test-코드히스토리 아이디를 조회하는 기능", description = "유저아이디와 코드 아이디를 기반으로 히스토리아이디 조회")
    @GetMapping("id")
    public ResponseEntity<?> getCodeHistory(
            @RequestHeader("UserId") String userId,
            @RequestParam Long codeId) {
        try {
            // 히스토리 조회
            Optional<Long> historyId = codeHistoryService.getHistoryId(userId, codeId);

            if (historyId.isPresent()) {
                // 히스토리 ID 반환
                return ResponseEntity.ok(historyId.get());
            } else {
                // 히스토리가 없으면 null 반환
                return ResponseEntity.ok(null);
            }
        } catch (Exception e) {
            // 오류 처리
            return ResponseEntity.status(500).body("히스토리 조회 중 오류가 발생했습니다.");
        }
    }

    // 특정 유저의 히스토리 조회
    @Operation(summary = "특정 유저의 히스토리 조회,히스토리는 아직 필터링불가", description = "특정 유저의 문제 풀이 히스토리를 페이징하여 조회")
    @GetMapping("/user")
    public ResponseEntity<Page<CodeHistoryDto>> getUserHistory(
            @RequestHeader("UserId") String userId,
            @PageableDefault(page = 0, size = 10) Pageable pageable) {

        try {
            // 서비스 호출로 유저 히스토리 조회
            Page<CodeHistoryDto> historyDtos = codeHistoryService.getUserHistory(userId, pageable);
            return ResponseEntity.ok(historyDtos);
        } catch (Exception e) {
            // 실패 시 400 상태 코드와 빈 페이지 반환
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Page.empty(pageable));
        }
    }
    //유저가 기존에 풀던 또는 풀었던 문제와 내용을 보여줌
    @Operation(summary = "풀던|이미푼 문제 이어풀기", description = "기존에 풀던 문제또는 이미 해결한 문제를 히스토리에서 불러옴")
    @GetMapping("/user/{codeId}")
    public ResponseEntity<CodeHistoryDto> getHistoryById(
            @PathVariable Long codeId, @RequestHeader("UserId") String userId) {

        // codeId로 Code 객체 조회
        Code code = codeRepository.findById(codeId).orElse(null);

        if (code == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // Code가 없으면 404 반환
        }

        // CodeHistoryDto 조회
        CodeHistoryDto codeHistoryDto = codeHistoryService.getCodeHistoryByUserIdAndCode(userId, code);

        // codeHistoryDto가 null인지 확인하고 상태에 맞는 응답 반환
        if (codeHistoryDto != null) {
            return ResponseEntity.ok(codeHistoryDto); // codeHistoryDto가 존재하면 200 OK와 함께 반환
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // codeHistoryDto가 없으면 404 반환
        }
    }

    // 유저가 컴파일 또는 문제제출 시 해당 문제를 히스토리에 저장 또는 갱신
    //userId를 참조해서 로그인한경우와 안한경우 구분해서 동작시키기 - x
    //필터적용해서 로그인 비로그인 구분하기 - ㅇ
    //프론트에서 구분하기로함

    }







