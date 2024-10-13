package com.codingtext.codebankservice.Service;

import com.codingtext.codebankservice.Dto.CodeDto;
import com.codingtext.codebankservice.Dto.CodeHistoryDto;
import com.codingtext.codebankservice.Entity.Code;
import com.codingtext.codebankservice.Entity.CodeHistory;
import com.codingtext.codebankservice.Repository.CodeHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CodeHistoryService {
    @Autowired
    private CodeHistoryRepository codeHistoryRepository;


    public List<CodeHistoryDto> getUserHistory(Long userId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);


        Page<CodeHistory> codeHistoryPage = codeHistoryRepository.findAllByUserId(userId, pageable);


        List<CodeHistoryDto> historyDtoList = new ArrayList<>();
        for (CodeHistory history : codeHistoryPage.getContent()) {
            CodeHistoryDto historyDto = CodeHistoryDto.ToDto(history);
            historyDtoList.add(historyDto);
        }

        return historyDtoList;
    }



}
