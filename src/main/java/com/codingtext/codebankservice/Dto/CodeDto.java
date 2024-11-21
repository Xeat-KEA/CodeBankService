package com.codingtext.codebankservice.Dto;

import com.codingtext.codebankservice.entity.Code;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Optional;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeDto {
    private Long codeId;
    private String title;
    private String content;
    private String difficulty;
    private String algorithm;
    private String registerStatus;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    private double correctRate;


    public static CodeDto toDto(Code code) {
        CodeDto codeDto = new CodeDto();
        codeDto.setCodeId(code.getCodeId());
        codeDto.setTitle(code.getTitle());
        codeDto.setContent(code.getContent());
        codeDto.setDifficulty(Optional.ofNullable(code.getDifficulty()).map(Enum::name).orElse("UNKNOWN"));
        codeDto.setAlgorithm(Optional.ofNullable(code.getAlgorithm()).map(Enum::name).orElse("UNKNOWN"));
        codeDto.setRegisterStatus(Optional.ofNullable(code.getRegisterStatus()).map(Enum::name).orElse("UNKNOWN"));
        codeDto.setCreatedAt(code.getCreatedAt());
        //codeDto.setCorrectRate(0.0);
        return codeDto;
    }
//public static CodeDto toDto(Code code) {
//    if (code == null) {
//        return null;
//    }
//
//    return CodeDto.builder()
//            .codeId(code.getCodeId())
//            .title(code.getTitle())
//            .content(code.getContent())
//            .difficulty(code.getDifficulty() != null ? code.getDifficulty().name() : "UNKNOWN")
//            .algorithm(code.getAlgorithm() != null ? code.getAlgorithm().name() : "UNKNOWN")
//            .registerStatus(code.getRegisterStatus() != null ? code.getRegisterStatus().name() : "UNKNOWN")
//            .createdAt(code.getCreatedAt())
//            .correctRate(0.0) // 기본값 설정
//            .build();
//}

//    public static CodeDto toDtoWithCorrectRate(Code code, double correctRate) {
//        CodeDto codeDto = toDto(code); // 기본 변환 호출
//        codeDto.setCorrectRate(correctRate); // 정답률 추가 설정
//        return codeDto;
//    }

}
