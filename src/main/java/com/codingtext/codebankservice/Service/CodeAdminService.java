package com.codingtext.codebankservice.Service;

import com.codingtext.codebankservice.Dto.CodeBank.CodeDto;
import com.codingtext.codebankservice.Dto.Compile.*;
import com.codingtext.codebankservice.Dto.User.UserInfoDto;
import com.codingtext.codebankservice.client.BaseResponse;
import com.codingtext.codebankservice.client.CompileServiceClient;
import com.codingtext.codebankservice.client.UserServiceClient;
import com.codingtext.codebankservice.entity.Algorithm;
import com.codingtext.codebankservice.entity.Code;
import com.codingtext.codebankservice.entity.Difficulty;
import com.codingtext.codebankservice.entity.RegisterStatus;
import com.codingtext.codebankservice.repository.CodeHistoryRepository;
import com.codingtext.codebankservice.repository.CodeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.http.client.params.ClientPNames;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CodeAdminService {
    //의존성주입
    private final CodeRepository codeRepository;
    private final CodeHistoryRepository codeHistoryRepository;
    private final CompileServiceClient compileServiceClient;
    private final CodeHistoryService codeHistoryService;
    private final UserServiceClient userServiceClient;



    public Page<Code> getCreatedProblems(Pageable pageable) {
        return codeRepository.findByRegisterStatus(RegisterStatus.REQUESTED, pageable);
    }
    @Transactional
    public CodeWithTestcases getCodeWithTestcases(Long codeId) {
        Code code = codeRepository.findById(codeId)
                .orElseThrow(() -> new RuntimeException("해당 ID의 문제가 존재하지 않습니다."));
        String encodedContent = Base64.getEncoder().encodeToString(code.getContent().getBytes());
        // Code 엔티티를 DTO로 변환하면서 content를 인코딩된 값으로 변환
        Code codeDecode = Code.builder()
                .codeId(code.getCodeId())
                .title(code.getTitle())
                .content(encodedContent)
                .difficulty(code.getDifficulty())
                .algorithm(code.getAlgorithm())
                .registerStatus(code.getRegisterStatus())
                .build();

        // CompileService로부터 Testcase 리스트 가져오기
        Integer id = codeId.intValue();
        //형식이 달라서 안됨 baseresponse만들것!
        BaseResponse<CodeIdWithTestcases> response = compileServiceClient.findCode(id);
        List<Testcase> testcases = response.getData().getTestcases();
        return new CodeWithTestcases(codeDecode, testcases);
    }

    @Transactional
    public Page<CodeWithTestcasesAndNickName> getPendingCodesWithTestcases(Pageable pageable) {
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt") // 내림차순 정렬
        );


        // 승인 대기 중인 문제들을 페이지네이션으로 조회
        //Page<Code> pendingCodes = codeRepository.findByRegisterStatus(RegisterStatus.REQUESTED, pageable);
        Page<Code> pendingCodes = codeRepository.findByRegisterStatus(RegisterStatus.REQUESTED, sortedPageable);

        // 각 문제에 대해 컴파일 서버에서 Testcase 리스트를 가져와 CodeWithTestcases로 변환
        List<CodeWithTestcasesAndNickName> codeWithTestcasesList = pendingCodes.getContent().stream()
                .map(code -> {
                    Integer id = code.getCodeId().intValue();
                    //형식이 달라서 안됨 baseresponse만들것!
                    BaseResponse<CodeIdWithTestcases> response = compileServiceClient.findCode(id);
                    List<Testcase> testcases = response.getData().getTestcases();
                    //List<Testcase> testcases = compileServiceClient.getTestcasesByCodeId(code.getCodeId());
                    Long codeId = code.getCodeId();
                    Long historyId = codeHistoryService.getAiHistoryId(codeId);
                    String userId = codeHistoryService.getUserId(historyId);
                    ResponseEntity<UserInfoDto> userInfo = userServiceClient.getUserInfo(userId);
                    System.out.println("codeId: "+codeId);
                    System.out.println("userId: "+userId);
                    String nickname = userInfo.getBody().getNickName();
                    System.out.println("nickname: "+nickname);
                    // content를 Base64로 인코딩
                    String encodedContent = Base64.getEncoder().encodeToString(code.getContent().getBytes());

                    // CodeWithTestcasesAndNickName 객체 생성
                    Code encodedCode = code.toBuilder().content(encodedContent).build(); // content 인코딩



                    return new CodeWithTestcasesAndNickName(nickname, encodedCode, testcases);
                })
                .collect(Collectors.toList());

        // 페이지 정보를 유지하면서 CodeWithTestcases의 페이지 객체 반환
        return new PageImpl<>(codeWithTestcasesList, pageable, pendingCodes.getTotalElements());
    }
    @Transactional
    public ResponseEntity<?> updateCode(Long codeId, String title, String content,Algorithm algorithm,Difficulty difficulty) {
        Optional<Code> optionalCode = codeRepository.findById(codeId);

        if (optionalCode.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Code with ID " + codeId + " not found");
        }

        Code code = optionalCode.get();
        code.setTitle(title);
        code.setContent(content);
        code.setAlgorithm(algorithm);
        code.setDifficulty(difficulty);
        code.setCreatedAt(LocalDateTime.now());

        // 변경 내용을 강제로 병합
        codeRepository.save(code);

        return ResponseEntity.ok("Code updated successfully");
    }
    @Transactional
    public ResponseEntity<?> editCode(Long codeId, String title, String content,Algorithm algorithm,Difficulty difficulty) {
        Optional<Code> optionalCode = codeRepository.findById(codeId);

        if (optionalCode.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Code with ID " + codeId + " not found");
        }

        Code code = optionalCode.get();
        code.setTitle(title);
        code.setContent(content);
        code.setAlgorithm(algorithm);
        code.setDifficulty(difficulty);

        // 변경 내용을 강제로 병합
        codeRepository.save(code);

        return ResponseEntity.ok("Code updated successfully");
    }

    public Long createCode(CodeWithTestcases codeWithTestcases){
        Code newCode = Code.builder()
                .title(codeWithTestcases.getCode().getTitle())
                .content(codeWithTestcases.getCode().getContent())
                .algorithm(codeWithTestcases.getCode().getAlgorithm())
                .difficulty(codeWithTestcases.getCode().getDifficulty())
                .createdAt(LocalDateTime.now())
                .registerStatus(RegisterStatus.REGISTERED)
                .build();
        codeRepository.save(newCode);
        System.out.println("newCodeId= "+newCode.getCodeId());
        return newCode.getCodeId();
    }

    //코드와 테스트케이스를 분리해서 테스트케이스를 컴파일서버로 보내는 코드
    public ResponseEntity<?> saveTestcase(CodeWithTestcases codeWithTestcases, Integer id) {
        // Testcase 객체 생성
        CodeIdWithTestcases codeIdWithTestcase = new CodeIdWithTestcases();
        codeIdWithTestcase.setId(id);
        codeIdWithTestcase.setTestcases(codeWithTestcases.getTestcases());

        // 컴파일 서버로 요청 보내기
        compileServiceClient.saveCode(codeIdWithTestcase);

        return ResponseEntity.ok("저장성공");
    }
    public ResponseEntity<?> saveTestcaseForEdit(CodeWithTestcasesForEdit codeWithTestcasesForEdit, Integer id) {
        // Testcase 객체 생성
        CodeIdWithTestcases codeIdWithTestcase = new CodeIdWithTestcases();
        codeIdWithTestcase.setId(id);
        codeIdWithTestcase.setTestcases(codeWithTestcasesForEdit.getTestcases());

        // 컴파일 서버로 요청 보내기
        compileServiceClient.saveCode(codeIdWithTestcase);

        return ResponseEntity.ok("저장성공");
    }


//    @Transactional
//    public boolean updateCodeWithStatus(Long codeId, String codeContent, String title, String status) {
//        try {
//            // 문제 상태 업데이트
//            codeRepository.updateRegisterStatusById(codeId, status);
//
//            // 문제 데이터 업데이트
//            codeRepository.updateCodeData(codeId, codeContent, title);
//
//            return true; // 성공적으로 업데이트 완료
//        } catch (Exception e) {
//            // 예외 로그 출력
//            System.err.println("코드 업데이트 오류: " + e.getMessage());
//            return false; // 업데이트 실패
//        }
//    }
}
