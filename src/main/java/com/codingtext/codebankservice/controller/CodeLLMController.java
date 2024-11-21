package com.codingtext.codebankservice.controller;

import com.codingtext.codebankservice.Dto.CodeDto;
import com.codingtext.codebankservice.Service.CodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Code LLM API", description = "LLM을 통해 문제를 생성하는 api")
@RestController
@RequiredArgsConstructor
@RequestMapping("/code/LLM")
public class CodeLLMController {
    private final CodeService codeService;

    // GPT 문제 생성 및 post 요청으로 저장
    //프론트에서 알고리즘,난이도,상세 요구사항(선택)을 받아서 llm서비스로 전달
    // title,content,algorithm,difficulty,registerstatus=created,createdAt + 테스트케이스를 받은후 문제를 따로 분리후 저장,
    // testcase를 저장하면서 생성된 문제id와 함께 compile서버로 전송
    @Operation(summary = "GPT로 문제 생성/아직구현안됨", description = "GPT로 생성된 문제를 저장하는 역할을 수행 아직 사용불가 추후 개선")
    @PostMapping("/gpt/create")
    public ResponseEntity<CodeDto> createGptCode(@RequestBody CodeDto codedto){

        try {
            CodeDto createdCode = codeService.createGptGeneratedCode(codedto.getTitle(), codedto.getContent(), codedto.getAlgorithm(), codedto.getDifficulty());
            return ResponseEntity.ok(createdCode);
        } catch (Exception e) {
            CodeDto emptyCode = new CodeDto();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(emptyCode);
        }
    }

    //에러코드 try catch방식 수정 advicor handler만들기



}
