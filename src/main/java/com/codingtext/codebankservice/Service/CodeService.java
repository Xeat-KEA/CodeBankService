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
        return codeRepository.findAll(pageable).map(CodeDto::toDto);
    }
    public CodeDto getCodeById(Long codeId) {
        Optional<Code> code = codeRepository.findById(codeId);

        if (code.isPresent()) {
            return CodeDto.toDto(code.get());
        } else {
            throw new IllegalArgumentException("Code not found with ID: " + codeId);
        }
    }



}
