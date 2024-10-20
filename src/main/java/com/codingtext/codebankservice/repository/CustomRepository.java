package com.codingtext.codebankservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.codingtext.codebankservice.entity.Code;

public interface CustomRepository {
    Page<Code> findCodesByAlgorithmAndDifficulty(String algorithm, String difficulty, Pageable pageable);
}
