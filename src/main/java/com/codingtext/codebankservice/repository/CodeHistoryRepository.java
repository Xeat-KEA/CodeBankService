package com.codingtext.codebankservice.repository;

import com.codingtext.codebankservice.entity.CodeHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CodeHistoryRepository extends JpaRepository<CodeHistory, Long> {
   /// Page<CodeHistory> findAllByUserId(Long userId);
   Page<CodeHistory> findAllByUserId(Long userId, Pageable pageable);

    //CodeHistory findByCodeIdAndUserId(Long codeId, Long userId);
}

