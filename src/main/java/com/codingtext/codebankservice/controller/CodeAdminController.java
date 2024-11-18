package com.codingtext.codebankservice.controller;

import com.codingtext.codebankservice.Dto.AdminResponse;
import com.codingtext.codebankservice.Dto.CodeDto;
import com.codingtext.codebankservice.Dto.CodeWithTestcases;
import com.codingtext.codebankservice.Service.CodeAdminService;
import com.codingtext.codebankservice.Service.CodeService;
import com.codingtext.codebankservice.client.CompileServiceClient;
import com.codingtext.codebankservice.client.AdminServiceClient;
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
    private final AdminServiceClient adminServiceClient;
    /*@Autowired
    public CodeAdminController(CodeRepository codeRepository,CompileServiceClient compileServiceClient,CodeService codeService,CodeAdminService codeAdminService,AdminServiceClient adminServiceClient){
        this.codeRepository = codeRepository;
        this.compileServiceClient = compileServiceClient;
        this.codeService = codeService;
        this.codeAdminService = codeAdminService;
        this.adminServiceClient = adminServiceClient;
    }*/

    //승인대기중인 문제조회
    //승인대기중인 문제들의 대한 전체목록,컴파일 서버에서 testcase를 받아와 함께 admin으로 제공
    @Operation(summary = "승인 대기중인 문제목록 조회", description = "승인 대기중인 문제목록을 조회")
    @GetMapping("/register/pending")
    public ResponseEntity<Page<CodeWithTestcases>> getPendingApprovalCodesWithTestcases(Pageable pageable) {
        try {
            // 서비스 호출로 승인 대기중인 문제 조회
            Page<CodeWithTestcases> pendingCodesWithTestcases = codeAdminService.getPendingCodesWithTestcases(pageable);
            return ResponseEntity.ok(pendingCodesWithTestcases);
        } catch (Exception e) {
            // 실패 시 400 상태 코드와 오류 메시지 반환
            Page<CodeWithTestcases> emptyPage = Page.empty(pageable);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(emptyPage);
        }
    }

    //정식등록요청(codebank->admin)
    //client가 코드로 요청 -> codebank에서 기존에있던 ai생성문제+testcase admin에게 보냄
    //created -> REQUESTED 상태로 바꿔주는 역할
    @Operation(summary = "문제 정식등록 요청 보내기", description = "ai를 통해 생성한 문제를 정식등록하기위해 admin으로 등록요청을 보냄")
    @PostMapping("/register/{codeId}")
    public ResponseEntity<CodeWithTestcases> sendRegisterStatus(@PathVariable Long codeId){
        try {
            if (codeRepository.existsById(codeId)) {
                codeRepository.updateRegisterStatusById(codeId, "REQUESTED");
               // CodeWithTestcases codeWithTestcases = codeAdminService.getCodeWithTestcases(codeId);
                //전송성공
                //전송성공시 뭘해야 admin에게 알릴수있을까?
                return ResponseEntity.status(HttpStatus.OK).body(null);
            } else {
                //전송실패 or 코드가없음 or 상태변환실패
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(null);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null);
        }
    }
    //정식등록요청(admin->codebank)->승인됨
    @Operation(summary = "문제 정식등록 요청 승인 후 상태 저장", description = "AI를 통해 생성한 문제를 정식 등록하기 위해 보낸 요청의 답을 받아 응답하기")
    @PutMapping("/register/{codeId}/permit")
    public ResponseEntity<String> updateRegisterStatus(@PathVariable Long codeId) {
        try {
            // 어드민 서비스에서 문제 데이터와 테스트케이스 가져오기
            ResponseEntity<AdminResponse> response = adminServiceClient.getCodeWithTestcasesFromAdmin(codeId);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                AdminResponse adminResponse = response.getBody();

                // 테스트케이스를 컴파일 서버로 전송
                compileServiceClient.sendTestcases(codeId, adminResponse.getTestcases());

                // 로컬 데이터베이스에 코드 업데이트
                codeRepository.updateRegisterStatusById(codeId, "REGISTERED");
                codeRepository.updateCodeData(codeId, adminResponse.getCodeContent(), adminResponse.getTitle());

                return new ResponseEntity<>("문제가 성공적으로 등록되었습니다.", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("어드민 서비스에서 문제 데이터를 가져올 수 없습니다.", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("문제 업데이트 중 오류 발생: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    //정식등록요청(admin->codebank)->거부됨
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
    @DeleteMapping("/delete/{codeId}")
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

    //요청된 문제 확인
    @Operation(summary = "승인 대기중인 문제 조회", description = "승인 대기중인 문제의 정보를 조회")
    @GetMapping("/register/pending/{codeId}")
    public ResponseEntity<CodeWithTestcases> getPendingApprovalCodesWithTestcases(@PathVariable Long codeId) {
        try {
            // 서비스 호출로 승인 대기중인 문제 조회
            CodeWithTestcases codeWithTestcases = codeAdminService.getCodeWithTestcases(codeId);
            return ResponseEntity.ok(codeWithTestcases);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }


}
