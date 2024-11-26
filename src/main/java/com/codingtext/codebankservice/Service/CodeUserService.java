package com.codingtext.codebankservice.Service;

import com.codingtext.codebankservice.entity.Difficulty;
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
        List<Difficulty> solvedDifficulties = codeHistoryRepository.findDistinctCodeDifficultyByUserIdAndIsCorrectTrue(userId);

        // 난이도별 점수를 합산
        int userPoint = solvedDifficulties.stream()
                .mapToInt(difficulty -> difficultyScoreMap.getOrDefault(difficulty, 0))
                .sum();

        return userPoint;
    }
    //특정유저의 문제를 푼횟수(정답오답 상관없이)

}
