package com.codingtext.codebankservice.Service;

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
    public int calculateUserPoint(String userId){
        //난이도와 점수 매핑
        Map<String, Integer> difficultyScoreMap = Map.of(
                "LEVEL1", 10,
                "LEVEL2", 20,
                "LEVEL3", 30,
                "LEVEL4", 40,
                "LEVEL5", 50
        );
        // 유저가 푼 문제의 난이도 목록 가져오기
        List<String> solvedDifficulties = codeHistoryRepository.findDistinctCodeDifficultyByUserIdAndIsCorrectTrue(userId);

        //문제를 푼횟수 * 10 말고 난이도에따라 레벨1=10,레벨2=20으로 계산해서 넘길것
        // 난이도별 점수를 합산
        int userPoint = solvedDifficulties.stream()
                .mapToInt(difficulty -> difficultyScoreMap.getOrDefault(difficulty, 0))
                .sum();

        return userPoint;
    }
    //특정유저의 문제를 푼횟수(정답오답 상관없이)

}
