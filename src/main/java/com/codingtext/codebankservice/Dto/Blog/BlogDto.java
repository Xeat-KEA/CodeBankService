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
    //코드 아이디
    private Long codeId;
    //코드 내용
    private String content;
    //코드 제목
    private String title;
    //유저가 작성한 코드
    private String writtenCode;

}
