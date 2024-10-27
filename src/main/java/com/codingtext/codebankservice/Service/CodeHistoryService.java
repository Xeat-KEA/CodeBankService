package com.codingtext.codebankservice.Service;

import com.codingtext.codebankservice.Dto.CodeHistoryDto;
import com.codingtext.codebankservice.entity.Code;
import com.codingtext.codebankservice.entity.CodeHistory;
import com.codingtext.codebankservice.entity.RegisterStatus;
import com.codingtext.codebankservice.repository.CodeHistoryRepository;
import com.codingtext.codebankservice.repository.CodeRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CodeHistoryService {
    @Autowired
    private CodeHistoryRepository codeHistoryRepository;
    @Autowired
    private CodeRepository codeRepository;


    public List<CodeHistoryDto> getUserHistory(Long userId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));


        Page<CodeHistory> codeHistoryPage = codeHistoryRepository.findAllByUserId(userId, pageable);


        List<CodeHistoryDto> historyDtoList = new ArrayList<>();
        for (CodeHistory history : codeHistoryPage.getContent()) {
            CodeHistoryDto historyDto = CodeHistoryDto.ToDto(history);
            historyDtoList.add(historyDto);
        }

        return historyDtoList;
    }
    @Transactional
    public void updateOrAddHistory(CodeHistoryDto historyRequest) {
        Optional<CodeHistory> history = codeHistoryRepository.findByCode_CodeIdAndUserId(
                historyRequest.getCodeId(), historyRequest.getUserId());

        if (history.isPresent()) {
            // 풀이 기록이 있으면 compiledAt 갱신
            CodeHistory existingHistory = history.get();
            CodeHistory updatedHistory = existingHistory.toBuilder()
                    .compiledAt(LocalDateTime.now())
                    .build();
            codeHistoryRepository.save(updatedHistory);
        } else {
            // 코드 엔티티 조회
            Code code = codeRepository.findById(historyRequest.getCodeId())
                    .orElseThrow(() -> new EntityNotFoundException("해당 코드가 존재하지 않습니다."));
            // registerStatus에 따라 isCreatedByAi 값 설정
            boolean isCreatedByAi = !RegisterStatus.CREATED.equals(code.getRegisterStatus());
            // 새로운 CodeHistory 생성
            CodeHistory newHistory = CodeHistory.builder()
                    .code(code) // 조회한 Code 엔티티를 설정
                    .userId(historyRequest.getUserId())
                    .writtenCode(historyRequest.getWrittenCode())
                    .isCorrect(historyRequest.getIsCorrect())
                    .isCreatedByAI(isCreatedByAi)
                    .createdAt(LocalDateTime.now())
                    .compiledAt(LocalDateTime.now())
                    .build();

            codeHistoryRepository.save(newHistory);
        }
    }



}
