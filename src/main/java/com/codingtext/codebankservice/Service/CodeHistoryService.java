package com.codingtext.codebankservice.Service;

import com.codingtext.codebankservice.Dto.CodeBank.CodeHistoryDto;
import com.codingtext.codebankservice.entity.*;
import com.codingtext.codebankservice.repository.CodeHistoryRepository;
import com.codingtext.codebankservice.repository.CodeRepository;
import com.codingtext.codebankservice.repository.CustomRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service

public class CodeHistoryService {

    private final CodeHistoryRepository codeHistoryRepository;


    @Qualifier("codeRepository") // 'codeRepository' Bean을 주입
    private final CodeRepository codeRepository;

    @Qualifier("customRepository") // 'customRepository' Bean을 주입
    private final CustomRepository customRepository;
    public CodeHistoryService(CodeHistoryRepository codeHistoryRepository,
                              @Qualifier("codeRepository") CodeRepository codeRepository,
                              @Qualifier("customRepository") CustomRepository customRepository) {
        this.codeHistoryRepository = codeHistoryRepository;
        this.codeRepository = codeRepository;
        this.customRepository = customRepository;
    }


    //히스토리 아이디 탐색
    public Optional<Long> getHistoryId(String userId, Long codeId) {

        return codeHistoryRepository.findCodeHistoryIdByUserIdAndCodeId(userId, codeId);
    }
    public Long getAiHistoryId(Long codeId){
        //자꾸 중복으로 2개이상 가져오네
        return codeHistoryRepository.findCodeHistoryIdByCodeId(codeId);
    }
    //ai생성문제등 해당 코드아이디에대해 히스토리가 유니크할때만 사용
    public String getUserId(Long HistoryId){
        return codeHistoryRepository.findUserIdByCodeHistoryId(HistoryId);
    }

    @Transactional
    public Long createContentEmptyHistory(String userId, Long codeId) {
        // 코드 엔티티를 조회
        Code code = codeRepository.findById(codeId)
                .orElseThrow(() -> new EntityNotFoundException("해당 코드가 존재하지 않습니다."));
        // registerStatus에 따라 isCreatedByAi 값 설정
        boolean isCreatedByAi = RegisterStatus.CREATED.equals(code.getRegisterStatus());

        // CodeHistory 객체 생성
        CodeHistory codeHistory = CodeHistory.builder()
                .code(code) // Code 엔티티 설정
                .userId(userId)
                .writtenCode("hello world!") // 초기에는 작성된 코드가 없음
                .isCorrect(false) // 초기 상태는 정답이 아님
                .isCreatedByAI(isCreatedByAi) // AI 문제 여부 초기화
                .createdAt(LocalDateTime.now()) // 생성 시간
                .compiledAt(LocalDateTime.now()) // 컴파일 시간은 초기화되지 않음
                .build();

        // 저장 후 저장된 객체 반환
        CodeHistory savedHistory = codeHistoryRepository.save(codeHistory);
        System.out.println("Saved CodeHistory ID: " + savedHistory.getCodeHistoryId());


        // 생성된 히스토리 ID 반환
        return savedHistory.getCodeHistoryId();
    }



    /*public Page<CodeHistoryDto> getUserHistory(String userId, Pageable pageable) {

        // Repository 호출로 Page 객체 가져오기
        Page<CodeHistory> codeHistoryPage = codeHistoryRepository.findAllByUserId(userId, pageable);

        // DTO 변환 처리
        return codeHistoryPage.map(history -> CodeHistoryDto.builder()
                .codeHistoryId(history.getCodeHistoryId())
                .codeId(history.getCode().getCodeId())
                .userId(history.getUserId())
                .writtenCode(history.getWrittenCode())
                .isCorrect(history.getIsCorrect())
                .isCreatedByAI(history.getIsCreatedByAI())
                .createdAt(history.getCreatedAt())
                .compiledAt(history.getCompiledAt())
                .build());
    }*/
    public Page<CodeHistoryDto>
    getFilteredUserHistories(String userId,
                                                      List<String> algorithms,
                                                      List<String> difficulties,
                                                      String searchBy,
                                                      String searchText,
                                                      Pageable pageable) {
        return customRepository.findUserHistoriesWithFilterAndSearch(
                userId, algorithms, difficulties, searchBy, searchText, pageable);
    }
    @Transactional
    public void updateOrAddHistory(CodeHistoryDto historyRequest,String userId) {
        CodeHistory history = codeHistoryRepository.findByCode_CodeIdAndUserId(historyRequest.getCodeId(), historyRequest.getUserId());

        if (history != null && history.getCodeHistoryId() != null) {
            String decodedContent;
            byte[] decodedBytes = Base64.getDecoder().decode(historyRequest.getWrittenCode());
            decodedContent = new String(decodedBytes);
            // 풀이 기록이 있으면 compiledAt,작성코드내용,정답여부 갱신
            //CodeHistory existingHistory = history.get();
            history.setCompiledAt(LocalDateTime.now());
            history.setWrittenCode(decodedContent);
            history.setIsCorrect(historyRequest.getIsCorrect());
            codeHistoryRepository.save(history);
        } else {
            // 풀이 기록이 없으면 히스토리를 생성 + 히스토리를 받아온 값으로 저장
            // 코드 엔티티 조회
            Code code = codeRepository.findById(historyRequest.getCodeId())
                    .orElseThrow(() -> new EntityNotFoundException("해당 코드가 존재하지 않습니다."));
            // registerStatus에 따라 isCreatedByAi 값 설정
            boolean isCreatedByAi = RegisterStatus.CREATED.equals(code.getRegisterStatus());

            String decodedContent;
            byte[] decodedBytes = Base64.getDecoder().decode(historyRequest.getWrittenCode());
            decodedContent = new String(decodedBytes);

            // 새로운 CodeHistory 생성
            CodeHistory codeHistory = CodeHistory.builder()
                    .code(code) // Code 엔티티 설정
                    .userId(userId)
                    .writtenCode(decodedContent) // 초기에는 작성된 코드가 없음,문제를 컴파일안하고 바로 제출할경우
                    .isCorrect(historyRequest.getIsCorrect()) // 초기 상태는 정답이 아님,문제를 컴파일안하고 바로 제출할경우
                    .isCreatedByAI(isCreatedByAi) // AI 문제 여부 초기화
                    .createdAt(LocalDateTime.now()) // 생성 시간
                    .compiledAt(LocalDateTime.now()) // 컴파일 시간은 초기화되지 않음
                    .build();
            codeHistoryRepository.save(codeHistory);


        }
    }


    public CodeHistoryDto getCodeHistoryByUserIdAndCode(String userId, Code code) {
        Optional<CodeHistory> codeHistoryOptional = codeHistoryRepository.findByUserIdAndCode(userId, code);

        if (codeHistoryOptional.isEmpty()) {
            return null;
        }

        CodeHistory codeHistory = codeHistoryOptional.get();
        return CodeHistoryDto.ToDto(codeHistory);
    }




    //code에서 삭제된 문제를 참조하는 history삭제
    // 주기적으로 참조되지 않는 history 삭제 - 매일 자정에 실행
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void cleanUpOrphanedHistory() {
        List<Code> validCodes = codeRepository.findAll();
        codeHistoryRepository.deleteAllByCodeNotIn(validCodes);

    }



}
