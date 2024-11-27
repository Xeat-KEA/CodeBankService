package com.codingtext.codebankservice.Service;

import com.codingtext.codebankservice.Dto.CodeBank.CodeDto;
import com.codingtext.codebankservice.client.LLMServiceClient;
import com.codingtext.codebankservice.entity.Algorithm;
import com.codingtext.codebankservice.entity.Code;
import com.codingtext.codebankservice.entity.Difficulty;
import com.codingtext.codebankservice.entity.RegisterStatus;
import com.codingtext.codebankservice.repository.CodeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CodeLLMService {
    private final LLMServiceClient llmServiceClient;
    private final CodeRepository codeRepository;

    //프론트에서 요청을받고 값을받아서 페인클라이언트로 llm서비스에 문제생성 요청 전송
    //리턴값을 받아와서 문제를 저장하고 compile서비스로 생성된 코드id와 함께 페인클라이언트로 전송
    //프론트에 문제내용+코드아이디를 전송하고 해당 문제에대한 히스토리 생성
    // GPT 생성한 문제 저장
    @Transactional
    public CodeDto createGptGeneratedCode(String title, String content, String algorithm, String difficulty) {
        Code newCode = Code.builder()
                .title(title)
                .content(content)
                .algorithm(Algorithm.valueOf(algorithm.toUpperCase()))
                .difficulty(Difficulty.valueOf(difficulty.toUpperCase()))
                .createdAt(LocalDateTime.now())
                .registerStatus(RegisterStatus.CREATED)
                .build();

        return CodeDto.toDto(codeRepository.save(newCode));
    }
}
