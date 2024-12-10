package com.codingtext.codebankservice.controller;

import com.codingtext.codebankservice.Dto.AdminResponse;
import com.codingtext.codebankservice.Dto.Blog.RegisterRequestDto;
import com.codingtext.codebankservice.Dto.CodeBank.CodeDto;
import com.codingtext.codebankservice.Dto.Compile.CodeIdWithTestcases;
import com.codingtext.codebankservice.Dto.Compile.CodeWithTestcases;
import com.codingtext.codebankservice.Dto.Compile.CodeWithTestcasesAndNickName;
import com.codingtext.codebankservice.Dto.Compile.CodeWithTestcasesForEdit;
import com.codingtext.codebankservice.Dto.User.UserInfoDto;
import com.codingtext.codebankservice.Service.CodeAdminService;
import com.codingtext.codebankservice.Service.CodeHistoryService;
import com.codingtext.codebankservice.Service.CodeLLMService;
import com.codingtext.codebankservice.Service.CodeService;
import com.codingtext.codebankservice.client.BlogServiceClient;
import com.codingtext.codebankservice.client.CompileServiceClient;
import com.codingtext.codebankservice.client.UserServiceClient;
import com.codingtext.codebankservice.entity.Code;
import com.codingtext.codebankservice.entity.RegisterStatus;
import com.codingtext.codebankservice.repository.CodeHistoryRepository;
import com.codingtext.codebankservice.repository.CodeRepository;
import com.sun.source.tree.IntersectionTypeTree;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Base64;


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
    private final BlogServiceClient blogServiceClient;
    private final CodeHistoryService codeHistoryService;
    private final UserServiceClient userServiceClient;
    private final CodeHistoryRepository codeHistoryRepository;



    //승인대기중인 문제조회
    //승인대기중인 문제들의 대한 전체목록,컴파일 서버에서 testcase를 받아와 함께 admin으로 제공
    @Operation(summary = "승인 대기중인 문제목록 조회", description = "승인 대기중인 문제목록을 조회")
    @GetMapping("/register/pendinglists")
    public ResponseEntity<Page<CodeWithTestcasesAndNickName>> getPendingApprovalCodesWithTestcases(Pageable pageable) {
        //try {
            // 서비스 호출로 승인 대기중인 문제 조회
            Page<CodeWithTestcasesAndNickName> pendingCodesWithTestcases = codeAdminService.getPendingCodesWithTestcases(pageable);
            return ResponseEntity.ok(pendingCodesWithTestcases);
//        } catch (Exception e) {
//            // 실패 시 400 상태 코드와 오류 메시지 반환
//            Page<CodeWithTestcases> emptyPage = Page.empty(pageable);
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(emptyPage);
//        }
    }
    //요청된 문제 확인
    //문제를 testacse와함께 제공
    //문제를 건의한 건의자의 이름을 관리자에게 함꼐전달
    //문제 등록 table을 따로 만들지않음 이유?->병목현상=로드밸런싱,복잡도다운
    @Operation(summary = "승인 대기중인 문제 조회", description = "승인 대기중인 문제의 정보를 조회")
    @GetMapping("/register/pendinglists/{codeId}")
    public ResponseEntity<?> getPendingApprovalCodesWithTestcases(@PathVariable Long codeId) {
        //해당 코드아이디를 히스토리로 가진건 생성자밖에없음
        //히스토리가 유니크함->코드아이디로 히스토리 아이디를 찾아서 userId를 찾고 userId를 통해 user-service에서 닉네임을 보내주면됨
        Long historyId = codeHistoryService.getAiHistoryId(codeId);
        System.out.println("hisId: "+historyId);
        //String userId = codeHistoryService.getUserId(historyId);
        //System.out.println("userid:"+userId);
        //System.out.println("히스토리아이디,유저아이디="+historyId+","+userId);
        //try {
//            ResponseEntity<UserInfoDto> userInfo = userServiceClient.getUserInfo(userId);
//
//            String nickname = userInfo.getBody().getNickName();
//            System.out.println("nickname:"+nickname);

            // 서비스 호출로 승인 대기중인 문제 조회
        CodeWithTestcases codeWithTestcases = codeAdminService.getCodeWithTestcases(codeId);

        if(codeWithTestcases.getCode().getRegisterStatus()!=RegisterStatus.REQUESTED){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "해당 문제는 승인 대기 상태가 아닙니다.", "status", codeWithTestcases.getCode().getRegisterStatus()));

//            CodeWithTestcasesAndNickName codeWithTestcasesAndNickName = CodeWithTestcasesAndNickName.builder()
//                    .nickName(nickname)
//                    .codeWithTestcases(codeWithTestcases)
//                    .build();
//
//            System.out.println("닉네임="+nickname);
        }else{
            return ResponseEntity.ok(codeWithTestcases);
        }

//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
//        }
    }


    //정식등록요청(admin->codebank)->승인됨
    // 블로그 쪽으로 알리기 -> 블로그에서 알림 전송
    // 수정된 내용 코드에 반영하기 상태뿐만아니라 코드 내용까지 수정할수있어야함
    // 잘못된 codeid를 넣거나 하는 경우 코드 새로 생길수있으니 해당사항에대한 예외처리를할것
    @Operation(summary = "문제 정식등록 요청 승인 후 상태 저장", description = "AI를 통해 생성한 문제를 정식 등록하기 위해 보낸 요청의 답을 받아 응답하기")
    @PutMapping("/register/{codeId}/permit")
    public ResponseEntity<String> updateRegisterStatus(
            @PathVariable Long codeId,
            @RequestBody CodeWithTestcasesForEdit codeWithTestcasesForEdit) {

        // 어드민 서비스 응답 데이터 검증
        if (codeWithTestcasesForEdit == null || codeWithTestcasesForEdit.getTestcases() == null || codeWithTestcasesForEdit.getContent() == null) {
            return ResponseEntity.badRequest().body("유효하지 않은 어드민 서비스 응답입니다.");
        }
        // Base64로 인코딩된 content 디코딩
        String decodedContent;
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(codeWithTestcasesForEdit.getContent());
            decodedContent = new String(decodedBytes);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Content 디코딩 실패");
        }


        // 테스트케이스를 컴파일 서비스로 전송
        try {
            //compileServiceClient.updateTestcases(codeId, adminResponse.getTestcases());
            //adminResponse.getTestcases()를 codeIdwithTestcase에 넣어 compile서비스에 전송
            Integer id = codeWithTestcasesForEdit.getCodeId().intValue();
            // AdminResponse에서 데이터를 추출하여 CodeIdWithTestcases에 매핑
            CodeIdWithTestcases codeIdWithTestcase = new CodeIdWithTestcases();
            codeIdWithTestcase.setId(id);
            codeIdWithTestcase.setTestcases(codeWithTestcasesForEdit.getTestcases());

            compileServiceClient.saveCode(codeIdWithTestcase);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("테스트케이스 전송 실패");
        }

        // 데이터베이스 상태 업데이트
        try {
            codeRepository.updateRegisterStatusById(codeId, RegisterStatus.REGISTERED);
            //codeRepository.updateCodeData(codeId, adminResponse.getCodeContent(), adminResponse.getTitle());
            codeAdminService.updateCode(codeId,codeWithTestcasesForEdit.getTitle(),decodedContent,codeWithTestcasesForEdit.getAlgorithm(),codeWithTestcasesForEdit.getDifficulty());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("문제 데이터 업데이트 실패");
        }
        //블로그로 알림전송

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



    //admin 문제 관리를 위한 문제조회(정식등록된문제들만)
    @Operation(summary = "admin 전체 문제 관리(정식등록된문제들만)", description = "admin이 문제를 조회할수있다")
    @GetMapping("/codeLists")
    public ResponseEntity<Page<CodeDto>> getAllCodes(@RequestParam(required = false) List<String> algorithms,
                                                     @RequestParam(required = false) List<String> difficulties,
                                                     @RequestParam(required = false) String searchBy,  // 검색 기준 (예: title, codeId)
                                                     @RequestParam(required = false) String searchText, // 검색어
                                                     @RequestParam(required = false) String sortBy,     // 정렬 기준 (예: createdAt, correctRate)
                                                     @PageableDefault(page = 0, size = 10) Pageable pageable) {


        try {
            Page<CodeDto> codes = codeService.getFilteredAndSearchedCodes(algorithms, difficulties, searchBy, searchText, sortBy,RegisterStatus.REGISTERED, pageable);
            return ResponseEntity.ok(codes);
        } catch (Exception e) {
            Page<CodeDto> codes = codeService.getFilteredAndSearchedCodes(algorithms, difficulties, searchBy, searchText, sortBy,RegisterStatus.REGISTERED, pageable);
            return ResponseEntity.ok(codes);
        }
    }
    @Operation(summary = "전체문제중 일부 문제 상세조회", description = "전체 문제중 특정 문제를 상세 조회할수있다")
    @GetMapping("/codeLists/{codeId}")
    public ResponseEntity<CodeWithTestcases> getCodesWithTestcases(@PathVariable Long codeId) {

        CodeDto codeFor = codeService.getCodeById(codeId);
        RegisterStatus registerStatus = codeFor.getRegisterStatus();
        // 문제 상태가 created인 문제는 불러오면 안됨
        if(registerStatus != RegisterStatus.CREATED) {
            if (codeFor!=null) {
                CodeWithTestcases codeWithTestcases = codeAdminService.getCodeWithTestcases(codeId);
                System.out.println("success codeid=" + codeId);

                return ResponseEntity.ok(codeWithTestcases);

            } else if (codeFor == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            else {
                System.out.println("대실패 codeid=" + codeId);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            }
        }else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }


    //admin 문제 추가
    //admin이 생성한 문제를 받아옴 저장해야함 기존 codeid가 없음,어떻게 testcase를 컴파일서버로 보냄?
    //상태도 바꿔야함 수정하도록
    @Operation(summary = "admin문제추가-testcase별도 코드만 생성및저장", description = "admin이 문제 생성및 추가 요청")
    @PostMapping("/add")
    public ResponseEntity<CodeWithTestcases> createCodeByAdmin(@RequestBody CodeWithTestcases codeWithTestcases){

        try {
            //받은 문제를 저장하고 생성된 문제의 코드아이디 받아오기
            Integer newCodeId = codeAdminService.createCode(codeWithTestcases).intValue();
            String decodedContent;
            try {
                byte[] decodedBytes = Base64.getDecoder().decode(codeWithTestcases.getCode().getContent());
                decodedContent = new String(decodedBytes);
            } catch (IllegalArgumentException e) {
                System.out.println("디코딩실패");
                return ResponseEntity.badRequest().body(null);
            }
            Code codeDecode = Code.builder()
                    .codeId(codeWithTestcases.getCode().getCodeId())
                    .createdAt(codeWithTestcases.getCode().getCreatedAt())
                    .title(codeWithTestcases.getCode().getTitle())
                    .content(decodedContent)
                    .algorithm(codeWithTestcases.getCode().getAlgorithm())
                    .difficulty(codeWithTestcases.getCode().getDifficulty())
                    .build();

            CodeWithTestcases codeWithTestcasesDecode = CodeWithTestcases.builder()
                    .code(codeDecode)
                    .testcases(codeWithTestcases.getTestcases())
                    .build();

            //테스트케이스 분리후 코드아이디로 컴파일서버에 추가요청
            codeAdminService.saveTestcase(codeWithTestcasesDecode,newCodeId);

            return ResponseEntity.ok(codeWithTestcasesDecode);
        } catch (Exception e) {
            CodeWithTestcases emptyCode = new CodeWithTestcases(null,null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(emptyCode);
        }
    }

    //admin 문제 수정
    @Operation(summary = "문제 수정", description = "문제+테스트케이스를 불러오고 내용을 수정할수있다")
    @PutMapping("/edit/{codeId}")
    public ResponseEntity<String> updateCode(
            @PathVariable Long codeId,
            @RequestBody CodeWithTestcasesForEdit codeWithTestcasesForEdit) {

        String decodedContent;
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(codeWithTestcasesForEdit.getContent());
            decodedContent = new String(decodedBytes);
        } catch (IllegalArgumentException e) {
            System.out.println("디코딩실패");
            return ResponseEntity.badRequest().body(null);
        }
        CodeWithTestcasesForEdit codeWithTestcasesForEditDecode = CodeWithTestcasesForEdit.builder()
                .codeId(codeWithTestcasesForEdit.getCodeId())
                .title(codeWithTestcasesForEdit.getTitle())
                .algorithm(codeWithTestcasesForEdit.getAlgorithm())
                .content(decodedContent)
                .testcases(codeWithTestcasesForEdit.getTestcases())
                .build();

        // 테스트케이스를 컴파일 서비스로 전송
        try {
            //compileServiceClient.updateTestcases(codeId, adminResponse.getTestcases());
            //adminResponse.getTestcases()를 codeIdwithTestcase에 넣어 compile서비스에 전송
            Integer id = codeWithTestcasesForEdit.getCodeId().intValue();
            System.out.println("id:"+id);
            // Base64로 인코딩된 content 디코딩


            // AdminResponse에서 데이터를 추출하여 CodeIdWithTestcases에 매핑
            codeAdminService.saveTestcaseForEdit(codeWithTestcasesForEditDecode,id);


        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("테스트케이스 전송 실패");
        }

        // 데이터베이스 상태 업데이트
        try {

            //정식등록된 문제만 edit하기때문에 상태는 변화시킬필요없음
            //codeRepository.updateRegisterStatusById(codeId, codeWithTestcasesForEdit.getRegisterStatus());
            //codeRepository.updateCodeData(codeId, codeWithTestcases.getCode().getContent(), codeWithTestcases.getCode().getTitle());
            codeAdminService.editCode(codeId,codeWithTestcasesForEdit.getTitle(),codeWithTestcasesForEditDecode.getContent(),codeWithTestcasesForEdit.getAlgorithm(),codeWithTestcasesForEdit.getDifficulty());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("문제 데이터 업데이트 실패");
        }

        return ResponseEntity.ok("문제가 성공적으로 수정되었습니다.");
    }


    //admin문제삭제
    //특정문제삭제
    //문제를 삭제할려면 히스토리를 전부 삭제하고 문제를 삭제해야함
    //id를 참조하여 문제를 삭제 그리고 id를 컴파일서버에보내서 id를 참조하는 testcase를 삭제요청
    @Operation(summary = "문제삭제+testcase삭제요청-히스토리 삭제가 선행되어야함", description = "특정 codeId를 가진 문제를 삭제하고 해당 codeId를 참조하는 testcase를 삭제하는 요청을 compileservice로 보냄")
    @DeleteMapping("/delete/{codeId}")
    public ResponseEntity<String> deleteCode(@PathVariable Long codeId) {
        try {
            if (!codeRepository.existsById(codeId)) {
                return new ResponseEntity<>("해당 ID의 문제가 없습니다.", HttpStatus.BAD_REQUEST);
            }

            // 1. code_history에서 해당 codeId를 참조하는 모든 데이터 삭제
            try {
                codeHistoryRepository.deleteAllByCodeId(codeId);
            } catch (Exception e) {
                return new ResponseEntity<>("히스토리 삭제 중 오류 발생: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }

            // 2. code 테이블에서 데이터 삭제
            try {
                codeRepository.deleteById(codeId);
            } catch (Exception e) {
                return new ResponseEntity<>("문제 삭제 중 오류 발생: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }

            // 3. compileService로 testcase 삭제 요청
            try {
                Integer id = codeId.intValue();
                compileServiceClient.removeCode(id);
            } catch (Exception e) {
                return new ResponseEntity<>("테스트케이스 삭제 요청 중 오류 발생: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }

            return new ResponseEntity<>("삭제 성공", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("문제 삭제 처리 중 예상치 못한 오류 발생: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }







}
