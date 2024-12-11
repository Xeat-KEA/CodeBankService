package com.codingtext.codebankservice.controller;

import com.codingtext.codebankservice.Dto.CodeBank.CodeDto;
import com.codingtext.codebankservice.Dto.Compile.CodeIdWithTestcases;
import com.codingtext.codebankservice.Dto.Compile.CodeWithTestcases;
import com.codingtext.codebankservice.Dto.Compile.Testcase;
import com.codingtext.codebankservice.Dto.LLM.LLMIdResponse;
import com.codingtext.codebankservice.Dto.LLM.LLMRequestDTO;
import com.codingtext.codebankservice.Dto.LLM.LLMResponseDTO;
import com.codingtext.codebankservice.Service.CodeHistoryService;
import com.codingtext.codebankservice.Service.CodeLLMService;
import com.codingtext.codebankservice.Service.CodeService;
import com.codingtext.codebankservice.client.CompileServiceClient;
import com.codingtext.codebankservice.client.LLMServiceClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Tag(name = "Code LLM API", description = "LLM을 통해 문제를 생성하는 api")
@RestController
@RequiredArgsConstructor
@RequestMapping("/code/LLM")
public class CodeLLMController {
    private final CodeService codeService;
    private final LLMServiceClient llmServiceClient;
    private final CompileServiceClient compileServiceClient;
    private final CodeLLMService codeLLMService;
    private final CodeHistoryService codeHistoryService;





    // GPT 문제 생성 및 post 요청으로 저장
    // 프론트에서 알고리즘,난이도,상세 요구사항(선택)을 받아서 llm서비스로 전달
    // title,content,algorithm,difficulty,registerstatus=created,createdAt + 테스트케이스를 받은후 문제를 따로 분리후 저장,
    // testcase를 저장하면서 생성된 문제id와 함께 compile서버로 전송
    // 히스토리 생성
    @Operation(summary = "GPT로 문제 생성/아직구현안됨", description = "GPT로 생성된 문제를 저장하는 역할을 수행 아직 사용불가 추후 개선")
    @PostMapping("/gpt/create")
    public ResponseEntity<LLMIdResponse> createGptCode(@RequestBody LLMResponseDTO.CodeGenerateClientResponse gptResponse, @RequestHeader("UserId") String userId){

        try {

            if (gptResponse == null) {
                throw new IllegalStateException("GPT 응답이 비어 있습니다!");
            }

            System.out.println("gpt:"+gptResponse);
            // 새로운 문제를 DB에 저장
            CodeDto createdCode = codeLLMService.createGptGeneratedCode(
                    gptResponse.getTitle(),
                    gptResponse.getContent(),
                    gptResponse.getAlgorithm(),
                    gptResponse.getDifficulty().name()

            );
            // 생성된 codeId 가져오기
            Long codeId = createdCode.getCodeId();
            if (codeId == null) {
                throw new IllegalStateException("문제 저장 중 오류가 발생했습니다!");
            }
            System.out.println("codeId:"+codeId);
            //생성된 문제를 바탕으로 히스토리 생성
            Long hisId = codeHistoryService.createContentEmptyHistory(userId, codeId);
            if(hisId == null){
                throw new IllegalStateException("히스토리 생성 중 오류가 발생했습니다!");
            }
            System.out.println("hisId:"+hisId);

            // TestCaseSpec 리스트 분리
            List<LLMResponseDTO.CodeGenerateResponse.TestCaseSpec> testCases = gptResponse.getTestCases();

            // TestCaseSpec 변환 (LLM -> Compile DTO)
            List<Testcase> convertedTestCases = testCases.stream()
                    .map(testCaseSpec -> new Testcase(
                            testCaseSpec.getInput(),  // String 그대로 사용
                            testCaseSpec.getOutput() // String 그대로 사용
                    ))
                    .toList();

            // 컴파일러 서비스로 보낼 데이터 생성
            CodeIdWithTestcases compilerRequest = CodeIdWithTestcases.builder()
                    .id(Math.toIntExact(codeId))
                    .testcases(convertedTestCases)
                    .build();

            System.out.println("컴파일 저장중");
            // 컴파일러에게 testcase저장 요청
            //에러분기-저장실패
            compileServiceClient.saveCode(compilerRequest);
            System.out.println("컴파일 저장성공"+compilerRequest);
            //CodeDto createdCode = codeService.createGptGeneratedCode(codedto.getTitle(), codedto.getContent(), codedto.getAlgorithm(), codedto.getDifficulty());
            //return ResponseEntity.ok(createdCode);

            //무엇을 리턴?-> 생성된 문제 보여주기x,생성된 코드아이디+생성된 히스토리아이디 전달하
            //에러분기-코드생성/저장 실패
            LLMIdResponse llmIdResponse = LLMIdResponse.builder()
                    .codeId(codeId)
                    .historyId(hisId)
                    .build();

            return ResponseEntity.ok(llmIdResponse);

        } catch (Exception e) {
            CodeDto emptyCode = new CodeDto();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }




}
