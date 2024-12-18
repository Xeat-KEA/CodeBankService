package com.codingtext.codebankservice.Service;

import com.codingtext.codebankservice.Dto.CodeBank.CodeDto;
import com.codingtext.codebankservice.entity.Algorithm;
import com.codingtext.codebankservice.entity.Code;
import com.codingtext.codebankservice.entity.Difficulty;
import com.codingtext.codebankservice.entity.RegisterStatus;
import com.codingtext.codebankservice.repository.CodeHistoryRepository;
import com.codingtext.codebankservice.repository.CodeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
//import static jdk.internal.net.http.hpack.QuickHuffman.codes;


@Service
@RequiredArgsConstructor
public class CodeService {
    private final CodeRepository codeRepository;
    private final CodeHistoryRepository codeHistoryRepository;



    // 문제의 정답률을 계산하는 메서드
    public double calculateCorrectRate(Long codeId) {
        long totalAttempts = codeHistoryRepository.countByCode_CodeId(codeId);
        long correctAttempts = codeHistoryRepository.countByCode_CodeIdAndIsCorrectTrue(codeId);

        if (totalAttempts == 0) {
            return 0.0;  // 풀이 기록이 없는 경우 정답률 0%
        }

        return ((double) correctAttempts / totalAttempts) * 100;
    }




    // 필터링 및 정렬된 문제 목록 반환
    public Page<CodeDto> getFilteredAndSearchedCodes(List<String> algorithms,
                                                     List<String> difficulties,
                                                     String searchBy,
                                                     String searchText,
                                                     String sortBy,
                                                     RegisterStatus registerStatus,
                                                     Pageable pageable) {
        registerStatus = null;
        // 필터링된 문제 목록을 가져옴
        Page<Code> codes = codeRepository.findCodesWithFilterAndSearch(algorithms, difficulties, searchBy, searchText, sortBy, registerStatus, pageable);

        // 각 문제의 정답률을 계산하여 CodeDto로 변환
        List<CodeDto> codeDtos = codes.stream().map(code -> {
            long correctCount = codeHistoryRepository.countByCode_CodeIdAndIsCorrectTrue(code.getCodeId());
            double correctRate = calculateCorrectRate(code.getCodeId());
            System.out.println("codeId: "+code.getCodeId());
            System.out.println("correctCount: "+correctCount);

            // Base64로 content 인코딩
            String encodedContent = Base64.getEncoder().encodeToString(code.getContent().getBytes());

            CodeDto codeDto = CodeDto.toDto(code);
            codeDto.setCorrectRate(correctRate);
            codeDto.setCorrectCount(correctCount); // 정답 수 설정
            codeDto.setContent(encodedContent); // 인코딩된 content 설정
            return codeDto;
        }).collect(Collectors.toList());

        // 정렬 처리 (정답률 기준 정렬)
        if ("correctRate".equalsIgnoreCase(sortBy)) {
            codeDtos = codeDtos.stream()
                    .sorted((dto1, dto2) -> Double.compare(dto2.getCorrectRate(), dto1.getCorrectRate()))  // 정답률 기준으로 정렬
                    .collect(Collectors.toList());
        }

        return new PageImpl<>(codeDtos, pageable, codes.getTotalElements());
    }


    // 특정 문제 조회 시 정답률 포함
    public CodeDto getCodeById(Long codeId) {
        Optional<Code> codeOptional = codeRepository.findById(codeId);

        if (codeOptional.isPresent()) {
            Code code = codeOptional.get();
            CodeDto codeDto = CodeDto.toDto(code);
            codeDto.setCorrectRate(calculateCorrectRate(codeId));  // 정답률 계산 후 추가
            return codeDto;
        } else {
            throw new IllegalArgumentException("Code not found with ID: " + codeId);
        }
    }

    public Page<Code> getRegisteredCode(RegisterStatus registerStatus,Pageable pageable){
        return codeRepository.findCodeByRegisterStatus(registerStatus,pageable);
    }



//    // 문제 삭제 스케줄러
//    @Transactional
//    @Scheduled(cron = "0 0 0 * * *") // 매일 자정에 실행
//    //@Scheduled(initialDelay = 1000, fixedRate = 86400000)//서버 실행직후 1초후에 실행,매일 반복
//    public void deleteUnusedCreatedCodes() {
//        LocalDateTime threshold = LocalDateTime.now().minusHours(24); // 24시간이 지난 걸 확인하기위한 기준
//
//        // created 상태 + 생성된 지 24시간 + history에 기록이 없는 문제들 조회
//        List<Code> codesToDelete = codeRepository.findByRegisterStatusAndCreatedAtBefore(RegisterStatus.CREATED, threshold);
//
//        // 필터링 history
//        codesToDelete = codesToDelete.stream()
//                .filter(code -> !codeHistoryRepository.existsByCode_CodeId(code.getCodeId()))
//                .toList();
//
//        if (!codesToDelete.isEmpty()) {
//            codeRepository.deleteAll(codesToDelete);
//            System.out.println("number of deleted problems: " + codesToDelete.size());//log용
//        }
//    }
    public Page<Code> getPendingCodes(Pageable pageable) {
        return codeRepository.findByRegisterStatus("REQUESTED", pageable);
    }

}
