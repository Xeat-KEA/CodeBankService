package com.codingtext.codebankservice.controller;

import com.codingtext.codebankservice.Dto.Blog.RegisterRequestDto;
import com.codingtext.codebankservice.Dto.CodeBank.CodeHistoryDto;
import com.codingtext.codebankservice.Service.CodeHistoryService;
import com.codingtext.codebankservice.client.BlogServiceClient;
import com.codingtext.codebankservice.entity.*;
import com.codingtext.codebankservice.repository.CodeHistoryRepository;
import com.codingtext.codebankservice.repository.CodeRepository;
import com.codingtext.codebankservice.repository.CustomRepository;
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

@Tag(name = "Code History API", description = "사용자의 문제 풀이 히스토리를 관리하는 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/code/history")
public class CodeHistoryController {
    private final CodeRepository codeRepository;
    private final CodeHistoryRepository codeHistoryRepository;
    private final CodeHistoryService codeHistoryService;
    private final CustomRepository customRepository;
    private final BlogServiceClient blogServiceClient;


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
    @Operation(summary = "유저 히스토리 조회", description = "알고리즘, 난이도, 검색, 정렬을 적용하여 유저 히스토리를 조회")
    @GetMapping("/user")
    public ResponseEntity<Page<CodeHistoryDto>> getUserHistories(
            @RequestHeader("UserId") String userId,
            @RequestParam(required = false) List<String> algorithms,
            @RequestParam(required = false) List<String> difficulties,
            @RequestParam(required = false) String searchBy,
            @RequestParam(required = false) String searchText,
            @PageableDefault(page = 0, size = 10) Pageable pageable) {

        Page<CodeHistoryDto> histories = codeHistoryService.getFilteredUserHistories(
                userId, algorithms, difficulties, searchBy, searchText, pageable);

        return ResponseEntity.ok(histories);
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
    //정식등록요청(codebank->admin)
    //client가 코드로 요청 -> codebank에서 기존에있던 ai생성문제+testcase admin에게 보냄
    //created -> REQUESTED 상태로 바꿔주는 역할
    //유저가 풀었던 문제만 요청 가능
    @Operation(summary = "히스토리에서 ai로 생성한 문제 정식등록 요청 보내기", description = "ai를 통해 생성한 문제를 정식등록하기위해 문제의 등록상태를 created->REQUESTED로 변환, admin으로 등록요청을 보냄(요청미구현)")
    @PostMapping("/register/{codeId}")
    public ResponseEntity<String> sendRegisterStatus(@PathVariable Long codeId,@RequestHeader("UserId") String userId){
        try {
            boolean test = codeRepository.existsByCodeIdAndRegisterStatus(codeId, RegisterStatus.CREATED);
            System.out.println(test);
            //해당아이디의 코드와 히스토리를 불러온다
            Code code = codeRepository.findById(codeId).orElse(null);
            Optional<CodeHistory> codeHistory = codeHistoryRepository.findCodeHistoryByUserIdAndCode_CodeId(userId, codeId);
            //상태가 created인 경우에만 요청을 보내야함 이미 registered인 상태는 건들면안됨
            //created상태이고 히스토라가 존재할때
            if (codeRepository.existsByCodeIdAndRegisterStatus(codeId, RegisterStatus.CREATED)&&codeHistoryRepository.existsByUserIdAndCode_CodeId(userId, codeId)) {
                // 상태를 requested로 바꿈
                codeRepository.updateRegisterStatusById(codeId, RegisterStatus.REQUESTED);
                RegisterRequestDto registerRequestDto = RegisterRequestDto.builder()
                        .userId(codeHistory.get().getUserId())
                        .title(code.getTitle())
                        .build();
                //블로그로 알람을 위한 정보전달
                blogServiceClient.saveCodeNotice(registerRequestDto);
                //전송성공
                //전송성공시 뭘해야 user에게 알릴수있을까?
                //전송할때 유저 아이디 동봉해서 보내기x 상세코드를 열람할때 userid를 찾아서 보내기
                return ResponseEntity.status(HttpStatus.OK).body("신청되었습니다!");

            } else if(codeRepository.existsByCodeIdAndRegisterStatus(codeId, RegisterStatus.REGISTERED)){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("이미 등록 되어있는 문제입니다.");
            } else if(codeRepository.existsByCodeIdAndRegisterStatus(codeId, RegisterStatus.REQUESTED)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("이미 신청한 문제입니다.");
            }else {
                //전송실패 or 코드가없음 or 상태변환실패
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("등록에 실패했습니다.");
            }
        } catch (Exception e) {
            e.printStackTrace(); // 예외 상세 정보 출력
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("신청실패: " + e.getMessage()); // 예외 메시지 포함
        }
    }


    // 유저가 컴파일 또는 문제제출 시 해당 문제를 히스토리에 저장 또는 갱신
    //userId를 참조해서 로그인한경우와 안한경우 구분해서 동작시키기 - x
    //필터적용해서 로그인 비로그인 구분하기 - ㅇ
    //프론트에서 구분하기로함

    }







