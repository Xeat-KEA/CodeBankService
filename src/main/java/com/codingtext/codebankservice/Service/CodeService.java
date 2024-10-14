package com.codingtext.codebankservice.Service;

import com.codingtext.codebankservice.Dto.CodeDto;
import com.codingtext.codebankservice.Entity.Algorithm;
import com.codingtext.codebankservice.Entity.Code;
import com.codingtext.codebankservice.Entity.Difficulty;
import com.codingtext.codebankservice.Entity.RegisterStatus;
import com.codingtext.codebankservice.Repository.CodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;


@Service
public class CodeService {
    @Autowired
    private CodeRepository codeRepository;

    //전체문제조회
    public Page<CodeDto> getAllCodes(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return codeRepository.findAll(pageable).map(CodeDto::toDto);
    }
    //특정문제조회
    public CodeDto getCodeById(Long codeId) {
        Optional<Code> code = codeRepository.findById(codeId);

        if (code.isPresent()) {
            return CodeDto.toDto(code.get());
        } else {
            throw new IllegalArgumentException("Code not found with ID: " + codeId);
        }
    }
    // GPT 생성 문제 저장
    public CodeDto createGptGeneratedCode(String title, String content, String algorithm, String difficulty) {
        Code newCode = new Code();
        newCode.setTitle(title);
        newCode.setContent(content);
        newCode.setAlgorithm(Algorithm.valueOf(algorithm));
        newCode.setDifficulty(Difficulty.valueOf(difficulty));
        newCode.setRegisterStatus(RegisterStatus.CREATED); // 기본값 CREATED
        newCode.setCreatedAt(LocalDateTime.now());
        return CodeDto.toDto(codeRepository.save(newCode));
    }



}
