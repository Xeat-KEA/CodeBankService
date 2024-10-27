package com.codingtext.codebankservice.repository;

import com.codingtext.codebankservice.entity.CodeHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CodeHistoryRepository extends JpaRepository<CodeHistory, Long> {
   // Page<CodeHistory> findAllByUserId(Long userId);
   Page<CodeHistory> findAllByUserId(Long userId, Pageable pageable);
   // 특정 문제의 총 풀이 횟수
   long countByCode_CodeId(Long codeId);
   // 특정 문제의 정답 횟수
   long countByCode_CodeIdAndIsCorrectTrue(Long codeId);
    //CodeHistory findByCodeIdAndUserId(Long codeId, Long userId);

    // 특정 문제에 대한 풀이 기록이 있는지 확인하는 메서드
    boolean existsByCode_CodeId(Long codeId);

    // 특정 문제 ID와 사용자 ID로 풀이 기록 조회
    Optional<CodeHistory> findByCode_CodeIdAndUserId(Long codeId, Long userId);

}

