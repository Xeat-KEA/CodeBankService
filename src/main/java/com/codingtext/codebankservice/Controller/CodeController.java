package com.codingtext.codebankservice.Controller;

import com.codingtext.codebankservice.Dto.CodeDto;
import com.codingtext.codebankservice.Entity.Code;
import com.codingtext.codebankservice.Service.CodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/code")
@RequiredArgsConstructor
public class CodeController {

    private final CodeService codeService;

    //전체문제조회+페이지처리
    @GetMapping("/lists")
    public ResponseEntity<Page<CodeDto>> getAllCodes(@RequestParam(defaultValue = "0") int page,@RequestParam(defaultValue = "10") int size){
        return ResponseEntity.ok(codeService.getAllCodes(page, size));
    }
    @GetMapping("/lists/{codeId}")
    public ResponseEntity<CodeDto> getCodeById(@PathVariable Long codeId) {
        return ResponseEntity.ok(codeService.getCodeById(codeId));
    }


}
