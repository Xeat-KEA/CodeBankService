package com.codingtext.codebankservice.repository;


import com.codingtext.codebankservice.entity.Algorithm;
import com.codingtext.codebankservice.entity.Code;
import com.codingtext.codebankservice.entity.Difficulty;
import com.codingtext.codebankservice.entity.RegisterStatus;
import feign.Param;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CodeRepository extends JpaRepository<Code, Long>, CustomRepository {
    Page<Code> findAll(Pageable pageble);
    List<Code> findByRegisterStatusAndCreatedAtBefore(RegisterStatus registerStatus, LocalDateTime dateTime);
    List<Code> findAll();
    @Transactional
    @Modifying
    @Query("UPDATE Code c SET c.registerStatus = :status WHERE c.codeId = :codeId")
    void updateRegisterStatusById(@Param("codeId") Long codeId, @Param("status") RegisterStatus status);


    Page<Code> findByRegisterStatus(RegisterStatus registerStatus, Pageable pageable);

    Page<Code> findByRegisterStatus(String registerStatus, Pageable pageable);

    @Modifying
    @Query("UPDATE Code c SET c.title = :title, c.content = :content WHERE c.codeId = :codeId")
    void updateCodeData(@Param("codeId") Long codeId, @Param("content") String content, @Param("title") String title);

    // codeId가 존재하고 status가 CREATED인지 확인
    boolean existsByCodeIdAndRegisterStatus(Long codeId, RegisterStatus registerStatus);




}

