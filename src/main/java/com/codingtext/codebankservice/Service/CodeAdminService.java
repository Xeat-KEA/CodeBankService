package com.codingtext.codebankservice.Service;

import com.codingtext.codebankservice.Dto.CodeDto;
import com.codingtext.codebankservice.Dto.CodeWithTestcases;
import com.codingtext.codebankservice.Dto.Testcase;
import com.codingtext.codebankservice.client.CompileServiceClient;
import com.codingtext.codebankservice.entity.Algorithm;
import com.codingtext.codebankservice.entity.Code;
import com.codingtext.codebankservice.entity.Difficulty;
import com.codingtext.codebankservice.entity.RegisterStatus;
import com.codingtext.codebankservice.repository.CodeHistoryRepository;
import com.codingtext.codebankservice.repository.CodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CodeAdminService {
    //의존성주입
    private final CodeRepository codeRepository;
    private final CodeHistoryRepository codeHistoryRepository;
    private final CompileServiceClient compileServiceClient;

    @Autowired
    public CodeAdminService(CodeRepository codeRepository, CodeHistoryRepository codeHistoryRepository, CompileServiceClient compileServiceClient) {
        this.codeRepository = codeRepository;
        this.codeHistoryRepository = codeHistoryRepository;
        this.compileServiceClient = compileServiceClient;
    }

    public Page<Code> getCreatedProblems(Pageable pageable) {
        return codeRepository.findByRegisterStatus(RegisterStatus.REQUESTED, pageable);
    }
    public CodeWithTestcases getCodeWithTestcases(Long codeId) {
        Code code = codeRepository.findById(codeId)
                .orElseThrow(() -> new RuntimeException("해당 ID의 문제가 존재하지 않습니다."));

        // CompileService로부터 Testcase 리스트 가져오기
        List<Testcase> testcases = compileServiceClient.getTestcasesByCodeId(codeId);

        return new CodeWithTestcases(code, testcases);
    }

    //admin이 만든 문제 저장
    public CodeDto createAdminCode(String title, String content, String algorithm, String difficulty) {
        Code newCode = Code.builder()
                .title(title)
                .content(content)
                .algorithm(Algorithm.valueOf(algorithm.toUpperCase()))
                .difficulty(Difficulty.valueOf(difficulty.toUpperCase()))
                .createdAt(LocalDateTime.now())
                .registerStatus(RegisterStatus.REGISTERED)
                .build();

        return CodeDto.toDto(codeRepository.save(newCode));
    }
}
