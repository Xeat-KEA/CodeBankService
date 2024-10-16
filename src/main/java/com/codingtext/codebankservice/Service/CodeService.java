package com.codingtext.codebankservice.Service;

import com.codingtext.codebankservice.Dto.CodeDto;
import com.codingtext.codebankservice.entity.Algorithm;
import com.codingtext.codebankservice.entity.Code;
import com.codingtext.codebankservice.entity.Difficulty;
import com.codingtext.codebankservice.entity.RegisterStatus;
import com.codingtext.codebankservice.repository.CodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;


@Service
public class CodeService {
    @Autowired
    private CodeRepository codeRepository;

    //전체문제조회
    public Page<CodeDto> getAllCodes(int page, int size) {
        Pageable pageable = PageRequest.of(page, size,Sort.by(Sort.Direction.DESC, "createdAt"));
        return codeRepository.findAll(pageable).map(CodeDto::toDto);
    }
    // 알고리즘별 문제 필터링 조회
    /*public Page<CodeDto> getFilteredCodes(String algorithm, String difficulty, int page, int size) {
        Pageable pageable = PageRequest.of(page, size,Sort.by(Sort.Direction.DESC, "createdAt"));

        if (algorithm != null && difficulty != null) {
            // 알고리즘과 난이도 둘 다 선택된 경우
            Algorithm selectedAlgorithm = Algorithm.valueOf(algorithm.toUpperCase());
            Difficulty selectedDifficulty = Difficulty.valueOf(difficulty.toUpperCase());
            return codeRepository.findByAlgorithmAndDifficulty(selectedAlgorithm, selectedDifficulty, pageable).map(CodeDto::toDto);
        } else if (algorithm != null) {
            // 알고리즘만 선택된 경우
            Algorithm selectedAlgorithm = Algorithm.valueOf(algorithm.toUpperCase());
            return codeRepository.findByAlgorithm(selectedAlgorithm, pageable).map(CodeDto::toDto);
        } else if (difficulty != null) {
            // 난이도만 선택된 경우
            Difficulty selectedDifficulty = Difficulty.valueOf(difficulty.toUpperCase());
            return codeRepository.findByDifficulty(selectedDifficulty, pageable).map(CodeDto::toDto);
        } else {
            // 둘 다 선택되지 않은 경우 전체 조회
            return codeRepository.findAll(pageable).map(CodeDto::toDto);
        }
    }*/
    public Page<CodeDto> getFilteredCodes(String algorithm, String difficulty, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return codeRepository.findCodesByAlgorithmAndDifficulty(algorithm, difficulty, pageable).map(CodeDto::toDto);
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
