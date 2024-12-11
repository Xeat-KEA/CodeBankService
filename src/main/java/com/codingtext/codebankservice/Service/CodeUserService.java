package com.codingtext.codebankservice.Service;

import com.codingtext.codebankservice.entity.CodeHistory;
import com.codingtext.codebankservice.entity.Difficulty;
import com.codingtext.codebankservice.entity.RegisterStatus;
import com.codingtext.codebankservice.repository.CodeHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CodeUserService {
    private final CodeHistoryRepository codeHistoryRepository;
    //특정유저의 점수를 난이도에따라 계산
    public int calculateUserPoint(String userId) {
        // 난이도와 점수 매핑
        Map<Difficulty, Integer> difficultyScoreMap = Map.of(
                Difficulty.LEVEL1, 10,
                Difficulty.LEVEL2, 20,
                Difficulty.LEVEL3, 30,
                Difficulty.LEVEL4, 40,
                Difficulty.LEVEL5, 50
        );

        // 유저가 푼 문제의 난이도 목록 가져오기
        List<Difficulty> solvedDifficulties = codeHistoryRepository.findDistinctCodeDifficultyByUserIdAndIsCorrectTrueAndCode_RegisterStatus(userId,RegisterStatus.REGISTERED);

        // 난이도별 점수를 합산
        int userPoint = solvedDifficulties.stream()
                .mapToInt(difficulty -> difficultyScoreMap.getOrDefault(difficulty, 0))
                .sum();

        return userPoint;
    }
    //특정유저의 문제를 푼횟수(정답오답 상관없이)
    public int calculateUserCount(String userId) {
        // 유저가 풀었던 문제 중 등록된 문제 가져오기
        List<CodeHistory> solvedHistory = codeHistoryRepository.findCodeHistoryByUserIdAndCode_RegisterStatus(userId, RegisterStatus.REGISTERED);

        // 정답인 문제의 횟수 계산
        int count = (int) solvedHistory.stream()
                .filter(CodeHistory::getIsCorrect) // 정답 여부 필터링
                .count();

        return count;
    }


}
