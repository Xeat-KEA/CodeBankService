package com.codingtext.codebankservice.controller;

import com.codingtext.codebankservice.Dto.AdminResponse;
import com.codingtext.codebankservice.Dto.CodeBank.CodeDto;
import com.codingtext.codebankservice.Dto.Compile.CodeIdWithTestcases;
import com.codingtext.codebankservice.Dto.Compile.CodeWithTestcases;
import com.codingtext.codebankservice.Service.CodeAdminService;
import com.codingtext.codebankservice.Service.CodeLLMService;
import com.codingtext.codebankservice.Service.CodeService;
import com.codingtext.codebankservice.client.CompileServiceClient;
import com.codingtext.codebankservice.entity.RegisterStatus;
import com.codingtext.codebankservice.repository.CodeRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final CodeIdWithTestcases codeIdWithTestcases;



    //승인대기중인 문제조회
    //승인대기중인 문제들의 대한 전체목록,컴파일 서버에서 testcase를 받아와 함께 admin으로 제공
    @Operation(summary = "승인 대기중인 문제목록 조회", description = "승인 대기중인 문제목록을 조회")
    @GetMapping("/register/pendinglists")
    public ResponseEntity<Page<CodeWithTestcases>> getPendingApprovalCodesWithTestcases(Pageable pageable) {
        //try {
            // 서비스 호출로 승인 대기중인 문제 조회
            Page<CodeWithTestcases> pendingCodesWithTestcases = codeAdminService.getPendingCodesWithTestcases(pageable);
            return ResponseEntity.ok(pendingCodesWithTestcases);
//        } catch (Exception e) {
//            // 실패 시 400 상태 코드와 오류 메시지 반환
//            Page<CodeWithTestcases> emptyPage = Page.empty(pageable);
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(emptyPage);
//        }
    }
    //요청된 문제 확인
    //문제를 testacse와함께 제공
    @Operation(summary = "승인 대기중인 문제 조회", description = "승인 대기중인 문제의 정보를 조회")
    @GetMapping("/register/pendinglists/{codeId}")
    public ResponseEntity<CodeWithTestcases> getPendingApprovalCodesWithTestcases(@PathVariable Long codeId) {
        try {
            // 서비스 호출로 승인 대기중인 문제 조회
            CodeWithTestcases codeWithTestcases = codeAdminService.getCodeWithTestcases(codeId);
            return ResponseEntity.ok(codeWithTestcases);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    //정식등록요청(codebank->admin)
    //client가 코드로 요청 -> codebank에서 기존에있던 ai생성문제+testcase admin에게 보냄
    //created -> REQUESTED 상태로 바꿔주는 역할
    @Operation(summary = "문제 정식등록 요청 보내기", description = "ai를 통해 생성한 문제를 정식등록하기위해 문제의 등록상태를 created->REQUESTED로 변환, admin으로 등록요청을 보냄(요청미구현)")
    @PostMapping("/register/{codeId}")
    public ResponseEntity<String> sendRegisterStatus(@PathVariable Long codeId){
        try {
            boolean test = codeRepository.existsByCodeIdAndRegisterStatus(codeId, RegisterStatus.CREATED);
            System.out.println(test);
            //상태가 created인 경우에만 요청을 보내야함 이미 registered인 상태는 건들면안됨(아직안함)
            if (codeRepository.existsByCodeIdAndRegisterStatus(codeId, RegisterStatus.CREATED)) {
                codeRepository.updateRegisterStatusById(codeId, RegisterStatus.REQUESTED);
                //System.out.println("신청안료");
                //CodeWithTestcases codeWithTestcases = codeAdminService.getCodeWithTestcases(codeId);
                //전송성공
                //전송성공시 뭘해야 user에게 알릴수있을까?
                return ResponseEntity.status(HttpStatus.OK).body("신청되었습니다!");

            } else if(codeRepository.existsByCodeIdAndRegisterStatus(codeId, RegisterStatus.REGISTERED)){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("이미 등록 되어있는 문제입니다.");
            } else if(codeRepository.existsByCodeIdAndRegisterStatus(codeId, RegisterStatus.REQUESTED)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("이미 신청한 문제입니다.");
            }else {
                //전송실패 or 코드가없음 or 상태변환실패
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("등록에 실패했습니다.");
            }
        } catch (Exception e) {
            e.printStackTrace(); // 예외 상세 정보 출력
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("신청실패: " + e.getMessage()); // 예외 메시지 포함
      }
    }
    //정식등록요청(admin->codebank)->승인됨
    @Operation(summary = "문제 정식등록 요청 승인 후 상태 저장", description = "AI를 통해 생성한 문제를 정식 등록하기 위해 보낸 요청의 답을 받아 응답하기")
    @PutMapping("/register/{codeId}/permit")
    public ResponseEntity<String> updateRegisterStatus(
            @PathVariable Long codeId,
            @RequestBody AdminResponse adminResponse) {

        // 어드민 서비스 응답 데이터 검증
        if (adminResponse == null || adminResponse.getTestcases() == null || adminResponse.getCodeContent() == null) {
            return ResponseEntity.badRequest().body("유효하지 않은 어드민 서비스 응답입니다.");
        }

        // 테스트케이스를 컴파일 서비스로 전송
        try {
            //compileServiceClient.updateTestcases(codeId, adminResponse.getTestcases());
            //adminResponse.getTestcases()를 codeIdwithTestcase에 넣어 compile서비스에 전송
            Integer id = adminResponse.getCodeId().intValue();
            // AdminResponse에서 데이터를 추출하여 CodeIdWithTestcases에 매핑
            CodeIdWithTestcases codeIdWithTestcase = new CodeIdWithTestcases();
            codeIdWithTestcase.setId(id);
            codeIdWithTestcase.setTestcases(adminResponse.getTestcases());

            compileServiceClient.saveCode(codeIdWithTestcase);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("테스트케이스 전송 실패");
        }

        // 데이터베이스 상태 업데이트
        try {
            codeRepository.updateRegisterStatusById(codeId, RegisterStatus.REGISTERED);
            codeRepository.updateCodeData(codeId, adminResponse.getCodeContent(), adminResponse.getTitle());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("문제 데이터 업데이트 실패");
        }

        return ResponseEntity.ok("문제가 성공적으로 등록되었습니다.");
    }



    //정식등록요청(admin->codebank)->거부됨
    @Operation(summary = "문제 정식등록 요청 거부후 상태저장", description = "ai를 통해 생성한 문제를 정식등록하기위해 보낸 요청의 답을 받아 응답하기")
    @PutMapping("/register/{codeId}/refused")
    public ResponseEntity<String> refuseRegisterStatus(@PathVariable Long codeId){
        try {
            if (codeRepository.existsByCodeIdAndRegisterStatus(codeId, RegisterStatus.REQUESTED)) {
                codeRepository.updateRegisterStatusById(codeId, RegisterStatus.CREATED);//승인실패로 requested->created
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
                //feignclient는 주고받는 함수의 매핑되는 이름이 다르거나 타입이 다르면 전달이 되지않음
                //Long codeid -> Integer id로 변환해야 가능
                //특히 json의 경우 이름이 다르면 매핑시키지못함
                Integer id = codeId.intValue();
                compileServiceClient.removeCode(id);
                return new ResponseEntity<>("삭제 성공", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("해당 ID의 문제가 없습니다.", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("문제 삭제 중 오류 발생", HttpStatus.BAD_REQUEST);
        }
    }
    //admin 문제 추가
    //admin이 생성한 문제를 받아옴 저장해야함 기존 codeid가 없음,어떻게 testcase를 컴파일서버로 보냄?
    //상태도 바꿔야함 수정하도록
    @Operation(summary = "admin문제추가-testcase별도 코드만 생성및저장", description = "admin이 문제 생성및 추가 요청")
    @PostMapping("/add")
    public ResponseEntity<CodeDto> createCodeByAdmin(@RequestBody CodeDto codedto){

        try {
            CodeDto createdCode = codeAdminService.createCode(codedto.getTitle(), codedto.getContent(), codedto.getAlgorithm(), codedto.getDifficulty());
            return ResponseEntity.ok(createdCode);
        } catch (Exception e) {
            CodeDto emptyCode = new CodeDto();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(emptyCode);
        }
    }






}
