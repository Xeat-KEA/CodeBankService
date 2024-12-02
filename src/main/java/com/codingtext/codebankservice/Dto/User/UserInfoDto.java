package com.codingtext.codebankservice.Dto.User;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
public class UserInfoDto {
    String userId;
    String nickName;
    String profileUrl;
    String profileMessage;
    String codeLanguage;
    String tier;
    Long blogId;

}
