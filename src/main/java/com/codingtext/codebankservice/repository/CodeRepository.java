package com.codingtext.codebankservice.repository;


import com.codingtext.codebankservice.entity.Algorithm;
import com.codingtext.codebankservice.entity.Code;
import com.codingtext.codebankservice.entity.Difficulty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CodeRepository extends JpaRepository<Code, Long>, CustomRepository {
    Page<Code> findAll(Pageable pageble);
    /*//알고리즘으로 필터링
    Page<Code> findByAlgorithm(Algorithm algorithm,Pageable pageable);
     //난이도로 필터링
    Page<Code> findByDifficulty(Difficulty difficulty, Pageable pageable);
    //알고리즘과 난이도로 필터링
    Page<Code> findByAlgorithmAndDifficulty(Algorithm algorithm, Difficulty difficulty, Pageable pageable);*/



}

