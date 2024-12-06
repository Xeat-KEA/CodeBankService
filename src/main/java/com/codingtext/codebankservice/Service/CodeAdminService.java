package com.codingtext.codebankservice.Service;

import com.codingtext.codebankservice.Dto.CodeBank.CodeDto;
import com.codingtext.codebankservice.Dto.Compile.CodeIdWithTestcases;
import com.codingtext.codebankservice.Dto.Compile.CodeWithTestcases;
import com.codingtext.codebankservice.Dto.Compile.CodeWithTestcasesAndNickName;
import com.codingtext.codebankservice.Dto.Compile.Testcase;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
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

        // CompileService로부터 Testcase 리스트 가져오기
        Integer id = codeId.intValue();
        //형식이 달라서 안됨 baseresponse만들것!
        BaseResponse<CodeIdWithTestcases> response = compileServiceClient.findCode(id);
        List<Testcase> testcases = response.getData().getTestcases();
        return new CodeWithTestcases(code, testcases);
    }

    @Transactional
    public Page<CodeWithTestcasesAndNickName> getPendingCodesWithTestcases(Pageable pageable) {
        // 승인 대기 중인 문제들을 페이지네이션으로 조회
        Page<Code> pendingCodes = codeRepository.findByRegisterStatus(RegisterStatus.REQUESTED, pageable);


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

                    String nickname = userInfo.getBody().getNickName();



                    return new CodeWithTestcasesAndNickName(nickname, code, testcases);
                })
                .collect(Collectors.toList());

        // 페이지 정보를 유지하면서 CodeWithTestcases의 페이지 객체 반환
        return new PageImpl<>(codeWithTestcasesList, pageable, pendingCodes.getTotalElements());
    }
    @Transactional
    public CodeDto createCode(String title, String content, Algorithm algorithm, Difficulty difficulty) {
        Code newCode = Code.builder()
                .title(title)
                .content(content)
                .algorithm(algorithm)
                .difficulty(difficulty)
                .createdAt(LocalDateTime.now())
                .registerStatus(RegisterStatus.CREATED)
                .build();

        return CodeDto.toDto(codeRepository.save(newCode));
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
