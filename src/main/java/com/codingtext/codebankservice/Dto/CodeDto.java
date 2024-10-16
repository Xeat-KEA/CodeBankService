package com.codingtext.codebankservice.Dto;

import com.codingtext.codebankservice.entity.Code;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Optional;

@Getter
@Setter
public class CodeDto {
    private Long codeId;
    private String title;
    private String content;
    private String difficulty;
    private String algorithm;
    private String registerStatus;
    private LocalDateTime createdAt;



    public static CodeDto toDto(Code code) {
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
