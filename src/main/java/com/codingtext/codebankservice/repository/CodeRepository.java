package com.codingtext.codebankservice.repository;


import com.codingtext.codebankservice.entity.Algorithm;
import com.codingtext.codebankservice.entity.Code;
import com.codingtext.codebankservice.entity.Difficulty;
import com.codingtext.codebankservice.entity.RegisterStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CodeRepository extends JpaRepository<Code, Long>, CustomRepository {
    Page<Code> findAll(Pageable pageble);
    List<Code> findByRegisterStatusAndCreatedAtBefore(RegisterStatus registerStatus, LocalDateTime dateTime);



}

