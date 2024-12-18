package com.codingtext.codebankservice.Dto.Blog;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestDto {
    private String userId;
    private String title;
    private boolean admit;
}
