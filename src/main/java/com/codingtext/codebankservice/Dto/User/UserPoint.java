package com.codingtext.codebankservice.Dto.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserPoint {
    String userId;
    int score;
    int solvedCount;
}
