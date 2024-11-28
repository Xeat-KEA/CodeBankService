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
    //여기에 코드 제목 + 내용 한번에 넣기
    //제목 들여쓰기 후 코드 내용넣기
    private String content;
    //유저가 작성한 코드
    private String writtenCode;

}
