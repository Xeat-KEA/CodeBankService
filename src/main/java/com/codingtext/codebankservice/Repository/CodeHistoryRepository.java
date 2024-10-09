package com.codingtext.codebankservice.Repository;

import com.codingtext.codebankservice.Entity.CodeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CodeHistoryRepository extends JpaRepository<CodeHistory, Long> {
}

