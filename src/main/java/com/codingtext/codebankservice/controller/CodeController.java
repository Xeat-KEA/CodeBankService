package com.codingtext.codebankservice.controller;

import com.codingtext.codebankservice.Dto.CodeDto;
import com.codingtext.codebankservice.Service.CodeHistoryService;
import com.codingtext.codebankservice.Service.CodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/code")
@RequiredArgsConstructor
public class CodeController {

    private final CodeService codeService;

    //전체문제조회+페이지처리
    @GetMapping("/lists")
    public ResponseEntity<Page<CodeDto>> getAllCodes(@RequestParam(required = false) List<String> algorithm,
                                                     @RequestParam(required = false) List<String> difficulty,
                                                     @RequestParam(required = false) String searchBy,  // 검색 기준 (예: title, codeId)
                                                     @RequestParam(required = false) String searchText, // 검색어
                                                     @RequestParam(required = false) String sortBy,     // 정렬 기준 (예: createdAt, correctRate)
                                                     @PageableDefault(page = 0, size = 10) Pageable pageable) {

        // 알고리즘과 난이도를 List로 바꿈
        List<String> algorithms = algorithm != null ? algorithm : Collections.emptyList();
        List<String> difficulties = difficulty != null ? difficulty : Collections.emptyList();

        return ResponseEntity.ok(codeService.getFilteredAndSearchedCodes(algorithms, difficulties, searchBy, searchText, sortBy, pageable));
    }

    //특정문제조회
    @GetMapping("/lists/{codeId}")
    public ResponseEntity<CodeDto> getCodeById(@PathVariable Long codeId) {
        return ResponseEntity.ok(codeService.getCodeById(codeId));
    }
    // GPT 문제 post 요청으로 저장
    @PostMapping("/gpt/create")
    public ResponseEntity<CodeDto> createGptCode( @RequestBody CodeDto codedto){
        return ResponseEntity.ok(codeService.createGptGeneratedCode(codedto.getTitle(), codedto.getContent(), codedto.getAlgorithm(), codedto.getDifficulty()));
    }



}
