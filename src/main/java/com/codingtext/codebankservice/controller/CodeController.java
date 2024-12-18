package com.codingtext.codebankservice.controller;

import com.codingtext.codebankservice.Dto.CodeBank.CodeDto;
import com.codingtext.codebankservice.Dto.CodeBank.CodeContentWithHistoryAndHistoryIdAndIsCorrectAndDifficulty;
import com.codingtext.codebankservice.Dto.Compile.CodeIdWithTestcases;
import com.codingtext.codebankservice.Service.CodeHistoryService;
import com.codingtext.codebankservice.Service.CodeService;
//import com.codingtext.codebankservice.Util.JwtUtil;
import com.codingtext.codebankservice.client.BaseResponse;
import com.codingtext.codebankservice.client.CompileServiceClient;
import com.codingtext.codebankservice.entity.CodeHistory;
import com.codingtext.codebankservice.entity.RegisterStatus;
import com.codingtext.codebankservice.repository.CodeHistoryRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Base64;

@Tag(name = "Code API", description = "코딩 테스트 문제를 관리하는 API")
@RestController
@RequestMapping("/code")
@RequiredArgsConstructor
public class CodeController {

    private final CodeService codeService;
    private final CompileServiceClient compileServiceClient;
    private final CodeHistoryService codeHistoryService;
    private final CodeHistoryRepository codeHistoryRepository;

    @Operation(summary = "feignclient test용도", description = "컴파일서비에서 codeId에 따른 테스트케이스 조회")
    @GetMapping("/open/{codeId}")
    public ResponseEntity<BaseResponse<CodeIdWithTestcases>> TestOpenFeign(@PathVariable Long codeId) {
        Integer id = codeId.intValue();
        BaseResponse<CodeIdWithTestcases> testresponese =  compileServiceClient.findCode(id);
        return ResponseEntity.ok(testresponese);
    }


    //전체문제조회+페이지처리
    //로그인/비로그인 전부 조회 가능
    @Operation(summary = "전체 문제 조회", description = "알고리즘, 난이도, 검색, 정렬을 적용하여 전체 문제를 조회")
    @GetMapping("/lists")
    public ResponseEntity<Page<CodeDto>> getAllCodes(@RequestParam(required = false) List<String> algorithms,
                                                     @RequestParam(required = false) List<String> difficulties,
                                                     @RequestParam(required = false) String searchBy,  // 검색 기준 (예: title, codeId)
                                                     @RequestParam(required = false) String searchText, // 검색어
                                                     @RequestParam(required = false) String sortBy,     // 정렬 기준 (예: createdAt, correctRate)
                                                     @PageableDefault(page = 0, size = 10) Pageable pageable) {

        // 알고리즘과 난이도를 List로 바꿈
        //List<String> algorithms = algorithm != null ? algorithm : Collections.emptyList();
        //List<String> difficulties = difficulty != null ? difficulty : Collections.emptyList();

       // return ResponseEntity.ok(codeService.getFilteredAndSearchedCodes(algorithm, difficulty, searchBy, searchText, sortBy, pageable));
        try {
            Page<CodeDto> codes = codeService.getFilteredAndSearchedCodes(algorithms, difficulties, searchBy, searchText, sortBy, RegisterStatus.REGISTERED, pageable);
            // content를 Base64로 인코딩하여 변환


            return ResponseEntity.ok(codes);
        } catch (Exception e) {
//            Page<CodeDto> emptyPage = Page.empty();
//            System.out.println("뭔가잘못됨");
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(emptyPage);
            Page<CodeDto> codes = codeService.getFilteredAndSearchedCodes(algorithms, difficulties, searchBy, searchText, sortBy,RegisterStatus.REGISTERED, pageable);
            return ResponseEntity.ok(codes);
        }
    }

    //로그인-특정문제조회
    //로그인 안 했을때 토큰으로 필터링해서 로그인된 상태가아님->문제만 보여주기 컴파일(가능?,히스토리생성안하기),저장불가,gpt질의 불가
    //로그인 했을때 토큰으로 필터링 로그인->진입과 동시에 히스토리 생성 컴파일,저장버튼,gpt질의 기능 사용시 히스토리 갱신
    //히스토리를 생성한후 컴파일,gpt,저장 아무것도 안하면 스케줄러 돌려서 삭제하기
    //만약 created면 가져오지 않기
    @Operation(summary = "특정 문제 조회-로그인 유저 전용", description = "특정 문제의 상세 정보를 조회")
    @GetMapping("/lists/{codeId}")
    public ResponseEntity<CodeContentWithHistoryAndHistoryIdAndIsCorrectAndDifficulty> getCodeById(@PathVariable Long codeId, @RequestHeader("UserId") String userId) {

            // 히스토리 ID 조회
            // 히스토리가 없으면 생성
            // 어떤 문제에 대한 동일 유저에대한 히스토리가 중복으로 존재하면안됨
            Optional<Long> historyId = codeHistoryService.getHistoryId(userId, codeId);
            System.out.println("userid="+userId);
            System.out.println("historyid="+historyId);
            CodeDto codeForRegisterStatus = codeService.getCodeById(codeId);
            RegisterStatus registerStatus = codeForRegisterStatus.getRegisterStatus();
            // 문제 상태가 created인 문제는 불러오면 안됨
            // requested인 상태도 안됨
            // 정식등록된 문제만
            // 히스토리가 있다 -> 또 풀러왔네? -> 기존에 풀던 내용 넘기기
            // 히스토리가 없다 -> 처음이네? -> 히스토리생성
           if(registerStatus == RegisterStatus.REGISTERED) {
               CodeDto code = codeService.getCodeById(codeId);
               String encodedContent = Base64.getEncoder().encodeToString(code.getContent().getBytes());
//               code = code.toBuilder()
//                       .content(encodedContent)
//                       .build();

               if (historyId.isPresent()) {
                   // 또 풀러왔네
                   // 히스토리가 있으면 코드 정보 반환 + 기존에 풀었던 내용도 주기(히스토리)
                   Optional<CodeHistory> codeHistory = codeHistoryRepository.findCodeHistoryByUserIdAndCode_CodeId(userId,codeId);
                   System.out.println("sucess historyid=" + historyId);
                   String encodedWrittenCode = Base64.getEncoder().encodeToString(codeHistory.get().getWrittenCode().getBytes());
                   //llm서비스 채팅내역도 요청?
                   CodeContentWithHistoryAndHistoryIdAndIsCorrectAndDifficulty codeContentWithHistoryAndHistoryIdAndIsCorrectAndDifficulty = CodeContentWithHistoryAndHistoryIdAndIsCorrectAndDifficulty.builder()
                           .code_Content(encodedContent)
                           .codeHistory_writtenCode(encodedWrittenCode)
                           .historyId(codeHistory.get().getCodeHistoryId())
                           .isCorrect(codeHistory.get().getIsCorrect())
                           .difficulty(codeHistory.get().getCode().getDifficulty())
                           .build();

                   return ResponseEntity.ok(codeContentWithHistoryAndHistoryIdAndIsCorrectAndDifficulty);

               } else if (historyId.isEmpty()) {
                   // 처음이네
                   // 히스토리가 없으면 생성 후 코드 정보 반환 + 생성된 히스토리 내용 반환
                   System.out.println("sucess but no historyid=" + historyId);
                   //히스토리 생성후 히스토리아이디 반환
                   Long newHistoryId = codeHistoryService.createContentEmptyHistory(userId, codeId);
                   Optional<CodeHistory> codeHistory = codeHistoryRepository.findCodeHistoryByUserIdAndCode_CodeId(userId,codeId);
                   String encodedWrittenCode = Base64.getEncoder().encodeToString(codeHistory.get().getWrittenCode().getBytes());
                   CodeContentWithHistoryAndHistoryIdAndIsCorrectAndDifficulty codeContentWithHistoryAndHistoryIdAndIsCorrectAndDifficulty = CodeContentWithHistoryAndHistoryIdAndIsCorrectAndDifficulty.builder()
                           .code_Content(encodedContent)
                           .codeHistory_writtenCode(encodedWrittenCode)
                           .historyId(newHistoryId)
                           .isCorrect(codeHistory.get().getIsCorrect())
                           .build();
                   //CodeDto code = codeService.getCodeById(codeId);
                   return ResponseEntity.ok(codeContentWithHistoryAndHistoryIdAndIsCorrectAndDifficulty);
               }
               //return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 코드가 존재하지 않습니다.");
               else {
                   System.out.println("대실패 userid=" + userId);
                   return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
               }
           }else{
               return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
           }

    }


    //비로그인-특정문제조회
    @Operation(summary = "특정 문제 조회-비로그인 유저전용", description = "특정 문제의 상세 정보를 조회")
    @GetMapping("non/lists/{codeId}")
    public ResponseEntity<CodeDto> NongetCodeById(@PathVariable Long codeId) {
        CodeDto code = codeService.getCodeById(codeId);
        String encodedContent = Base64.getEncoder().encodeToString(code.getContent().getBytes());
        code = code.toBuilder()
                .content(encodedContent)
                .build();
        try {
            //CodeDto code = codeService.getCodeById(codeId);
            return ResponseEntity.ok(code);
        } catch (Exception e) {
            CodeDto emptyCode = new CodeDto();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(emptyCode);
        }
    }





}
