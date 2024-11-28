package com.codingtext.codebankservice.controller;

import com.codingtext.codebankservice.Dto.Blog.BlogDto;
import com.codingtext.codebankservice.Dto.CodeBank.CodeHistoryDto;
import com.codingtext.codebankservice.Service.CodeHistoryService;
import com.codingtext.codebankservice.entity.Code;
import com.codingtext.codebankservice.entity.CodeHistory;
import com.codingtext.codebankservice.repository.CodeHistoryRepository;
import com.codingtext.codebankservice.repository.CodeRepository;
import com.codingtext.codebankservice.repository.CustomRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Tag(name = "Code blog API", description = "블로그에게 코드 넘겨주기위한 api")
@RestController
@RequestMapping("/blog")
@RequiredArgsConstructor
public class CodeBlogController {

    private final CodeHistoryService codeHistoryService;
    private final CodeRepository codeRepository;
    private final CodeHistoryRepository codeHistoryRepository;
    @Operation(summary = "코드게시물 상세조회를 위한 api", description = "히스토리를 불러와서 code제목+내용+히스토리작성코드")
    @GetMapping("/user/{codeId}")
    public ResponseEntity<BlogDto> getHistoryWithCodeById(
            @PathVariable Long codeId, @RequestHeader("UserId") String userId) {

        // codeId로 Code 객체 조회
        Code code = codeRepository.findById(codeId).orElse(null);
        // CodeHistoryDto 조회
        Optional<CodeHistory> codeHistory = codeHistoryRepository.findCodeHistoryByUserIdAndCode_CodeId(userId, codeId);


        // 제목 + 내용
        //String formattedContent = code.getTitle() + "\n " + code.getContent();

        // BlogDto 빌더로 생성
        BlogDto blogDto = BlogDto.builder()
                .codeId(code.getCodeId())
                .title(code.getTitle())
                .content(code.getContent())
                .writtenCode(codeHistory.get().getWrittenCode())
                .build();


        if (code == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // Code가 없으면 404 반환
        }

        // codeHistoryDto가 null인지 확인하고 상태에 맞는 응답 반환
        if (codeHistory != null) {
            return ResponseEntity.ok(blogDto); // blogDto 반환
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // codeHistoryDto가 없으면 404 반환
        }
    }
}
