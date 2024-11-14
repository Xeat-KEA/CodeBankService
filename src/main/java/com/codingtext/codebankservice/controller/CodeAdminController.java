package com.codingtext.codebankservice.controller;

import com.codingtext.codebankservice.Dto.CodeDto;
import com.codingtext.codebankservice.Dto.CodeWithTestcases;
import com.codingtext.codebankservice.Service.CodeAdminService;
import com.codingtext.codebankservice.Service.CodeService;
import com.codingtext.codebankservice.client.CompileServiceClient;
import com.codingtext.codebankservice.entity.Code;
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

@Tag(name = "Code register API", description = "코딩 테스트 문제를 관리하는 API")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class CodeAdminController {

    private final CodeService codeService;
    private final CompileServiceClient compileServiceClient;
    private final CodeAdminService codeAdminService;
    private final CodeRepository codeRepository;
    @Autowired
    public CodeAdminController(CodeRepository codeRepository,CompileServiceClient compileServiceClient,CodeService codeService,CodeAdminService codeAdminService){
        this.codeRepository = codeRepository;
        this.compileServiceClient = compileServiceClient;
        this.codeService = codeService;
        this.codeAdminService = codeAdminService;
    }

    //승인대기중인 문제조회
    //승인대기중인 문제들의 대한 전체목록,컴파일 서버에서 testcase를 받아와 함께 admin으로 제공
    @Operation(summary = "승인 대기중인 문제 조회", description = "승인 대기중인 문제의 정보를 조회")
    @GetMapping("/register/pending")
    public ResponseEntity<Page<CodeWithTestcases>> getPendingApprovalCodesWithTestcases(Pageable pageable) {
        Page<CodeWithTestcases> pendingCodesWithTestcases = codeAdminService.getPendingCodesWithTestcases(pageable);
        return ResponseEntity.ok(pendingCodesWithTestcases);
    }

    //정식등록요청(codebank->admin)
    //client가 코드로 요청 -> 코드에서 기존에있던 ai생성문제+testcase admin에게 보냄
    //created -> REQUESTED 상태로 바꿔주는 역할
    @Operation(summary = "문제 정식등록 요청 보내기", description = "ai를 통해 생성한 문제를 정식등록하기위해 admin으로 등록요청을 보냄")
    @PostMapping("/register/{codeId}")
    public ResponseEntity<CodeWithTestcases> sendRegisterStatus(@PathVariable Long codeId){
        try {
            if (codeRepository.existsById(codeId)) {
                codeRepository.updateRegisterStatusById(codeId, "REQUESTED");
                CodeWithTestcases codeWithTestcases = codeAdminService.getCodeWithTestcases(codeId);
                return ResponseEntity.ok(codeWithTestcases);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(null);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null);
        }
    }

    //정식등록요청(admin->codebank) 승인됨
    //어드민으로 보낸 요청이 성공/실패 -> 코드로 돌아와서 적용+컴파일서버로 id와 함께 testcase를 보냄->어드민으로 결과 전송
    @Operation(summary = "문제 정식등록 요청 승인후 상태저장", description = "ai를 통해 생성한 문제를 정식등록하기위해 보낸 요청의 답을받아 응답하기")
    @PutMapping("/register/{codeId}/permit")
    public ResponseEntity<String> updateRegisterStatus(@PathVariable Long codeId){
        try {
            if (codeRepository.existsById(codeId)) {
                codeRepository.updateRegisterStatusById(codeId, "REGISTERED");
                compileServiceClient.createCompileData(codeId);//어드민에서 컴파일서버에서 테스트케이스 업데이트한경우 적용하기위함
                return new ResponseEntity<>("문제승인됨", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("해당 ID의 문제가 없습니다.", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("문제 업데이트 중 오류 발생", HttpStatus.BAD_REQUEST);
        }
    }
    //정식등록요청(admin->codebank) 거부됨
    @Operation(summary = "문제 정식등록 요청 거부후 상태저장", description = "ai를 통해 생성한 문제를 정식등록하기위해 보낸 요청의 답을 받아 응답하기")
    @PutMapping("/register/{codeId}/refused")
    public ResponseEntity<String> refuseRegisterStatus(@PathVariable Long codeId){
        try {
            if (codeRepository.existsById(codeId)) {
                codeRepository.updateRegisterStatusById(codeId, "CREATED");//승인실패로 requested->created
                //compileServiceClient.createCompileData(codeId);//어드민에서 컴파일서버에서 테스트케이스 업데이트한경우 적용하기위함
                return new ResponseEntity<>("승인거부됨", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("해당 ID의 문제가 없습니다.", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("문제 업데이트 중 오류 발생", HttpStatus.BAD_REQUEST);
        }
    }

    //특정문제삭제
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



}
