package com.codingtext.codebankservice.Repository;


import com.codingtext.codebankservice.Entity.Code;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CodeRepository extends JpaRepository<Code, Long> {
    Page<Code> findAll(Pageable pageble);

}

