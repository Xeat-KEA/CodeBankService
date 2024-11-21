package com.codingtext.codebankservice.controller;

import com.codingtext.codebankservice.Dto.CodeDto;
import com.codingtext.codebankservice.Dto.CodeResponseDto;
import com.codingtext.codebankservice.Service.CodeHistoryService;
import com.codingtext.codebankservice.Service.CodeService;
//import com.codingtext.codebankservice.Util.JwtUtil;
import com.codingtext.codebankservice.client.CompileServiceClient;
import com.codingtext.codebankservice.repository.CodeRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Tag(name = "Code API", description = "코딩 테스트 문제를 관리하는 API")
@RestController
@RequestMapping("/code")
@RequiredArgsConstructor
public class CodeController {

    private final CodeService codeService;
    private final CompileServiceClient compileServiceClient;
    private final CodeHistoryService codeHistoryService;
    private CodeRepository codeRepository;


    //전체문제조회+페이지처리
    @Operation(summary = "전체 문제 조회", description = "알고리즘, 난이도, 검색, 정렬을 적용하여 전체 문제를 조회")
    @GetMapping("/lists")
    public ResponseEntity<Page<CodeDto>> getAllCodes(@RequestParam(required = false) List<String> algorithm,
                                                     @RequestParam(required = false) List<String> difficulty,
                                                     @RequestParam(required = false) String searchBy,  // 검색 기준 (예: title, codeId)
                                                     @RequestParam(required = false) String searchText, // 검색어
                                                     @RequestParam(required = false) String sortBy,     // 정렬 기준 (예: createdAt, correctRate)
                                                     @PageableDefault(page = 0, size = 10) Pageable pageable) {

        // 알고리즘과 난이도를 List로 바꿈
        //List<String> algorithms = algorithm != null ? algorithm : Collections.emptyList();
        //List<String> difficulties = difficulty != null ? difficulty : Collections.emptyList();

       // return ResponseEntity.ok(codeService.getFilteredAndSearchedCodes(algorithm, difficulty, searchBy, searchText, sortBy, pageable));
        try {
            Page<CodeDto> codes = codeService.getFilteredAndSearchedCodes(algorithm, difficulty, searchBy, searchText, sortBy, pageable);
            return ResponseEntity.ok(codes);
        } catch (Exception e) {
//            Page<CodeDto> emptyPage = Page.empty();
//            System.out.println("뭔가잘못됨");
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(emptyPage);
            Page<CodeDto> codes = codeService.getFilteredAndSearchedCodes(algorithm, difficulty, searchBy, searchText, sortBy, pageable);
            return ResponseEntity.ok(codes);
        }
    }

    //특정문제조회
    //로그인 안 했을때 토큰으로 필터링해서 로그인된 상태가아님->문제만 보여주기 컴파일(가능?,히스토리생성안하기),저장불가,gpt질의 불가
    //로그인 했을때 토큰으로 필터링 로그인->진입과 동시에 히스토리 생성 컴파일,저장버튼,gpt질의 기능 사용시 히스토리 갱신
    //히스토리를 생성한후 컴파일,gpt,저장 아무것도 안하면 스케줄러 돌려서 삭제하기
    @Operation(summary = "특정 문제 조회-로그인 유저 전용", description = "특정 문제의 상세 정보를 조회")
    @GetMapping("/lists/{codeId}")
    public ResponseEntity<?> getCodeById(@PathVariable Long codeId, @RequestHeader("UserId") String userId) {
        try {
            // 히스토리 ID 조회
            Optional<Long> historyId = codeHistoryService.getHistoryId(userId, codeId);
            if (historyId.isPresent()) {
                // 히스토리가 있으면 코드 정보 반환
                CodeDto code = codeService.getCodeById(codeId);
                //프론트에 historyId도 반환하는가?
                return ResponseEntity.ok(code);
            } else {
                // 히스토리가 없으면 생성 후 코드 정보 반환
                Long newHistoryId = codeHistoryService.createHistory(userId, codeId);
                CodeDto code = codeService.getCodeById(codeId);
                return ResponseEntity.ok(code);
            }
        } catch (EntityNotFoundException e) {
            // 코드가 없는 경우 처리
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 코드가 존재하지 않습니다.");
        } catch (Exception e) {
            // 기타 예외 처리
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("문제를 처리하는 동안 오류가 발생했습니다.");
        }
    }

    //특정문제조회
    @Operation(summary = "특정 문제 조회-비로그인 유저전용", description = "특정 문제의 상세 정보를 조회")
    @GetMapping("non/lists/{codeId}")
    public ResponseEntity<CodeDto> NongetCodeById(@PathVariable Long codeId) {
        try {
            CodeDto code = codeService.getCodeById(codeId);
            return ResponseEntity.ok(code);
        } catch (Exception e) {
            CodeDto emptyCode = new CodeDto();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(emptyCode);
        }
    }





    //admin 문제 추가
    //admin이 생성한 문제를 받아옴 저장해야함 기존 codeid가 없음,어떻게 testcase를 컴파일서버로 보냄?
    //상태도 바꿔야함 수정하도록
    @Operation(summary = "admin문제추가/아직 구현안됨", description = "admin이 문제 생성및 추가 요청?")
    @PostMapping("/add")
    public ResponseEntity<CodeDto> createCodeByAdmin( @RequestBody CodeDto codedto){

        try {
            CodeDto createdCode = codeService.createGptGeneratedCode(codedto.getTitle(), codedto.getContent(), codedto.getAlgorithm(), codedto.getDifficulty());
            return ResponseEntity.ok(createdCode);
        } catch (Exception e) {
            CodeDto emptyCode = new CodeDto();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(emptyCode);
        }
    }



}
