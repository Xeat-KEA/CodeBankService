package com.codingtext.codebankservice.repository;

import com.codingtext.codebankservice.Dto.CodeBank.CodeHistoryDto;
import com.codingtext.codebankservice.entity.Code;
import com.codingtext.codebankservice.entity.CodeHistory;
import com.codingtext.codebankservice.entity.Difficulty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CodeHistoryRepository extends JpaRepository<CodeHistory, Long> {
   // Page<CodeHistory> findAllByUserId(Long userId);
   Page<CodeHistory> findAllByUserId(String userId, Pageable pageable);
   // 특정 문제의 총 풀이 횟수
   long countByCode_CodeId(Long codeId);
   // 특정 문제의 정답 횟수
   long countByCode_CodeIdAndIsCorrectTrue(Long codeId);
    //난이도 목록 조회
    @Query("SELECT DISTINCT c.code.difficulty FROM CodeHistory c WHERE c.userId = :userId AND c.isCorrect = true")
    List<Difficulty> findDistinctCodeDifficultyByUserIdAndIsCorrectTrue(String userId);



    //특정 유저의 정답횟수
    int countCodeHistoriesByUserIdAndIsCorrectTrue(String userId);

    //CodeHistory findByCodeIdAndUserId(Long codeId, Long userId);

    // 특정 문제에 대한 풀이 기록이 있는지 확인하는 메서드
    boolean existsByCode_CodeId(Long codeId);

    // 특정 문제 ID와 사용자 ID로 풀이 기록 조회
    Optional<CodeHistory> findByCode_CodeIdAndUserId(Long codeId, String userId);

    Optional<CodeHistory> findCodeHistoryByUserIdAndCode_CodeId(String userId, Long codeId);

    boolean existsByUserIdAndCode_CodeId(String userId, Long codeId);

    Optional<CodeHistory> findByUserIdAndCode(String userId, Code code);

    void deleteAllByCodeNotIn(List<Code> codes);

    //히스토리아이디 탐색
    @Query("SELECT h.codeHistoryId FROM CodeHistory h WHERE h.userId = :userId AND h.code.codeId = :codeId")
    Optional<Long> findCodeHistoryIdByUserIdAndCodeId(@Param("userId") String userId, @Param("codeId") Long codeId);






}

