package com.codingtext.codebankservice.Service;

import com.codingtext.codebankservice.Dto.CodeDto;
import com.codingtext.codebankservice.Dto.CodeIdWithTestcases;
import com.codingtext.codebankservice.Dto.CodeWithTestcases;
import com.codingtext.codebankservice.Dto.Testcase;
import com.codingtext.codebankservice.client.BaseResponse;
import com.codingtext.codebankservice.client.CompileServiceClient;
import com.codingtext.codebankservice.entity.Algorithm;
import com.codingtext.codebankservice.entity.Code;
import com.codingtext.codebankservice.entity.Difficulty;
import com.codingtext.codebankservice.entity.RegisterStatus;
import com.codingtext.codebankservice.repository.CodeHistoryRepository;
import com.codingtext.codebankservice.repository.CodeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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



    public Page<Code> getCreatedProblems(Pageable pageable) {
        return codeRepository.findByRegisterStatus(RegisterStatus.REQUESTED, pageable);
    }
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

    //admin이 만든 문제 저장
    public Page<CodeWithTestcases> getPendingCodesWithTestcases(Pageable pageable) {
        // 승인 대기 중인 문제들을 페이지네이션으로 조회
        Page<Code> pendingCodes = codeRepository.findByRegisterStatus("REQUESTED", pageable);

        // 각 문제에 대해 컴파일 서버에서 Testcase 리스트를 가져와 CodeWithTestcases로 변환
        List<CodeWithTestcases> codeWithTestcasesList = pendingCodes.getContent().stream()
                .map(code -> {
                    Integer id = code.getCodeId().intValue();
                    //형식이 달라서 안됨 baseresponse만들것!
                    BaseResponse<CodeIdWithTestcases> response = compileServiceClient.findCode(id);
                    List<Testcase> testcases = response.getData().getTestcases();
                    //List<Testcase> testcases = compileServiceClient.getTestcasesByCodeId(code.getCodeId());
                    return new CodeWithTestcases(code, testcases);
                })
                .collect(Collectors.toList());

        // 페이지 정보를 유지하면서 CodeWithTestcases의 페이지 객체 반환
        return new PageImpl<>(codeWithTestcasesList, pageable, pendingCodes.getTotalElements());
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
