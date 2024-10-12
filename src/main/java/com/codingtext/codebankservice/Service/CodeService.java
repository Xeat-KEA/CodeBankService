package com.codingtext.codebankservice.Service;

import com.codingtext.codebankservice.Dto.CodeDto;
import com.codingtext.codebankservice.Entity.Code;
import com.codingtext.codebankservice.Repository.CodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.Optional;


@Service
public class CodeService {
    @Autowired
    private CodeRepository codeRepository;

    //전체문제조회
    public Page<CodeDto> getAllCodes(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return codeRepository.findAll(pageable).map(this::convertToDto);
    }


    // 엔티티를 DTO로 변환
    private CodeDto convertToDto(Code code) {
        CodeDto codeDto = new CodeDto();
        codeDto.setCodeId(code.getCodeId());
        codeDto.setTitle(code.getTitle());
        codeDto.setContent(code.getContent());
        codeDto.setDifficulty(Optional.ofNullable(code.getDifficulty()).map(Enum::name).orElse("UNKNOWN"));
        codeDto.setAlgorithm(Optional.ofNullable(code.getAlgorithm()).map(Enum::name).orElse("UNKNOWN"));
        codeDto.setRegisterStatus(Optional.ofNullable(code.getRegisterStatus()).map(Enum::name).orElse("UNKNOWN"));
        codeDto.setCreatedAt(code.getCreatedAt());
        return codeDto;
    }
}
