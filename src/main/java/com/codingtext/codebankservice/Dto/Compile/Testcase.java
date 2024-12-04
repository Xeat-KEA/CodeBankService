package com.codingtext.codebankservice.Dto.Compile;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Testcase {
    private String input;
    private String output;
}

