package com.codingtext.codebankservice.Dto;

import lombok.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Component
public class CodeIdWithTestcases {
    private Integer id;
    private List<Testcase> testcases;
}
