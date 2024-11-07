package com.codingtext.codebankservice.controller;

import com.codingtext.codebankservice.Dto.CodeDto;
import com.codingtext.codebankservice.Service.CodeHistoryService;
import com.codingtext.codebankservice.Service.CodeService;
import com.codingtext.codebankservice.client.CompileServiceClient;
import com.codingtext.codebankservice.repository.CodeRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@Tag(name = "Code API", description = "코딩 테스트 문제를 관리하는 API")
@RestController
@RequestMapping("/code")
@RequiredArgsConstructor
public class CodeController {

    private final CodeService codeService;
    private final CompileServiceClient compileServiceClient;
    private CodeRepository codeRepository;
    @Autowired
    public CodeController(CodeRepository codeRepository,CompileServiceClient compileServiceClient,CodeService codeService){
        this.codeRepository = codeRepository;
        this.compileServiceClient = compileServiceClient;
        this.codeService = codeService;
    }


    //전체문제조회+페이지처리
    @Operation(summary = "전체 문제 조회", description = "알고리즘, 난이도, 검색, 정렬을 적용하여 전체 문제를 조회")
    @GetMapping("/lists")
    public ResponseEntity<Page<CodeDto>> getAllCodes(@RequestParam(required = false) List<String> algorithm,
                                                     @RequestParam(required = false) List<String> difficulty,
                                                     @RequestParam(required = false) String searchBy,  // 검색 기준 (예: title, codeId)
                                                     @RequestParam(required = false) String searchText, // 검색어
                                                     @RequestParam(required = false) String sortBy,     // 정렬 기준 (예: createdAt, correctRate)
                                                     @PageableDefault(page = 0, size = 10) Pageable pageable) {

        // 알고리즘과 난이도를 List로 바꿈
        //List<String> algorithms = algorithm != null ? algorithm : Collections.emptyList();
        //List<String> difficulties = difficulty != null ? difficulty : Collections.emptyList();

        return ResponseEntity.ok(codeService.getFilteredAndSearchedCodes(algorithm, difficulty, searchBy, searchText, sortBy, pageable));
    }

    //특정문제조회
    @Operation(summary = "특정 문제 조회", description = "특정 문제의 상세 정보를 조회")
    @GetMapping("/lists/{codeId}")
    public ResponseEntity<CodeDto> getCodeById(@PathVariable Long codeId) {
        return ResponseEntity.ok(codeService.getCodeById(codeId));
    }

    // GPT 문제 post 요청으로 저장
    //프론트에서 알고리즘,난이도,상세 요구사항(선택)을 받아서 llm서비스로 전달 llm에서
    // title,content,algorithm,difficulty,registerstatus=created,createdAt + 테스트케이스를 받은후 문제를 따로 분리후 저장,
    // testcase를 저장하면서 생성된 문제id와 함께 compile서버로 전송
    @Operation(summary = "GPT로 문제 생성", description = "GPT로 생성된 문제를 저장하는 역할을 수행 아직 사용불가 추후 개선")
    @PostMapping("/gpt/create")
    public ResponseEntity<CodeDto> createGptCode( @RequestBody CodeDto codedto){
        return ResponseEntity.ok(codeService.createGptGeneratedCode(codedto.getTitle(), codedto.getContent(), codedto.getAlgorithm(), codedto.getDifficulty()));
    }

    //id를 참조하여 문제를 삭제 그리고 id를 컴파일서버에보내서 id를 참조하는 testcase를 삭제요청
    @Operation(summary = "문제삭제+testcase삭제요청", description = "특정 codeId를 가진 문제를 삭제하고 해당 codeId를 참조하는 testcase를 삭제하는 요청을 compileservice로 보냄")
    @DeleteMapping("/{codeId}")
    public ResponseEntity<String> deleteCode(@PathVariable Long codeId) {
        try {
            if (codeRepository.existsById(codeId)) {
                codeRepository.deleteById(codeId);
                compileServiceClient.deleteCompileData(codeId);
                return new ResponseEntity<>("삭제 성공", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("해당 ID의 문제가 없습니다.", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("문제 삭제 중 오류 발생", HttpStatus.BAD_REQUEST);
        }
    }

    //client가 admin에게요청
    //admin으로부터 정식승인된 문제를 받아옴 기존에있던 ai생성문제를 수정,testcase를 컴파일서버로 id와 함께 보냄
    @PutMapping("/permit/{codeId}")
    public ResponseEntity<String> updateRegisterStatus(@PathVariable Long codeId){
        try {
            if (codeRepository.existsById(codeId)) {
                codeRepository.createById(codeId);
                compileServiceClient.createCompileData(codeId);
                return new ResponseEntity<>("삭제 성공", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("해당 ID의 문제가 없습니다.", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("문제 삭제 중 오류 발생", HttpStatus.BAD_REQUEST);
        }
    }
    //admin이 생성한 문제를 받아옴 저장해야함 기존 codeid가 없음,어떻게 testcase를 컴파일서버로 보냄?
    @PostMapping("/add")
    public ResponseEntity<CodeDto> createCodeByAdmin( @RequestBody CodeDto codedto){
        return ResponseEntity.ok(codeService.createGptGeneratedCode(codedto.getTitle(), codedto.getContent(), codedto.getAlgorithm(), codedto.getDifficulty()));
    }



}
