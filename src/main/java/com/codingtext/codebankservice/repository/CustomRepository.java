package com.codingtext.codebankservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.codingtext.codebankservice.entity.Code;

import java.util.List;

public interface CustomRepository {
    //Page<Code> findCodesByAlgorithmAndDifficulty(String algorithm, String difficulty, Pageable pageable);
    Page<Code> findCodesWithFilterAndSearch(List<String> algorithms, List<String> difficulties, String searchBy, String searchText, String sortBy, Pageable pageable);

}
