package com.codingtext.codebankservice.controller;

import com.codingtext.codebankservice.Dto.CodeDto;
import com.codingtext.codebankservice.Service.CodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/code")
@RequiredArgsConstructor
public class CodeController {

    private final CodeService codeService;

    //전체문제조회+페이지처리
    @GetMapping("/lists")
    public ResponseEntity<Page<CodeDto>> getAllCodes(@RequestParam(required = false) String algorithm,
                                                     @RequestParam(required = false) String difficulty,
                                                     @PageableDefault(page = 0, size = 10) Pageable pageable){

        return ResponseEntity.ok(codeService.getFilteredCodes(algorithm, difficulty,pageable.getPageNumber(), pageable.getPageSize()));
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
