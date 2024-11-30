package com.codingtext.codebankservice.repository;

import com.codingtext.codebankservice.Dto.CodeBank.CodeHistoryDto;
import com.codingtext.codebankservice.entity.Algorithm;
import com.codingtext.codebankservice.entity.Difficulty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.codingtext.codebankservice.entity.Code;
import org.springframework.stereotype.Repository;

import java.util.List;
public interface CustomRepository {
    //Page<Code> findCodesByAlgorithmAndDifficulty(String algorithm, String difficulty, Pageable pageable);
    Page<Code> findCodesWithFilterAndSearch(List<String> algorithms, List<String> difficulties, String searchBy, String searchText, String sortBy, Pageable pageable);
    Page<CodeHistoryDto> findUserHistoriesWithFilterAndSearch(String userId, List<String> algorithms, List<String> difficulties, String searchBy, String searchText, Pageable pageable);

}
