package com.codingtext.codebankservice.Service;

import com.codingtext.codebankservice.repository.CodeHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CodeUserService {
    private final CodeHistoryRepository codeHistoryRepository;
    //특정유저의 정답갯수를 세고 *10(임시 점수 산정방식)을 해서 반환
    public int calculateUserPoint(String userId){
        int userPoint = codeHistoryRepository.countCodeHistoriesByUserIdAndIsCorrectTrue(userId);
        return userPoint*10;
    }
    //특정유저의 문제를 푼횟수(정답오답 상관없이)

}
