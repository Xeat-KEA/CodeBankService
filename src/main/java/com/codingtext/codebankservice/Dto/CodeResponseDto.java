package com.codingtext.codebankservice.Dto;
import lombok.AllArgsConstructor;
import lombok.Data;
@Data
@AllArgsConstructor
public class CodeResponseDto {
    private CodeDto code;       // 문제 상세 정보
    private boolean isLoggedIn; // 로그인 여부
    private Long historyId;     // 히스토리 ID (로그인 상태에서만 반환)

}
