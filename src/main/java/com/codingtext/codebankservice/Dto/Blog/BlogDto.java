package com.codingtext.codebankservice.Dto.Blog;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlogDto {
    private Long codeId;
    private String content;
    private String writtenCode;
    //문제이름 히스토리 표시용
    private String codeTitle;
}
