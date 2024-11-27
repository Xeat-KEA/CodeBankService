package com.codingtext.codebankservice.Dto.LLM;

import com.codingtext.codebankservice.entity.Difficulty;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Builder
@Getter
@AllArgsConstructor
@Schema(name = "LLMResponseDTO", description = "LLM 응답 양식", title = "LLMResponseDTO [LLM 응답 양식]")
public class LLMResponseDTO {
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(name = "CodeGenerateResponse", description = "코딩테스트 생성 응답", title = "CodeGenerateResponse [코딩테스트 생성 응답]")
    public static class CodeGenerateResponse{
        @Schema(name = "title", description = "문제 제목", example = "혼자 남지 않기 위한 최적의 경로 찾기")
        private String title;
        @Schema(name = "algorithm", description = "알고리즘", example = "DP")
        private String algorithm;
        @Schema(name = "content", description = "문제 내용", example = "\"<h3>문제 설명:</h3>\\n<p>당신은 '1'과 '0'으로 이루어진 2D 이진 행렬에서 가장 큰 정사각형으로 '1'로 이루어진 배열을 찾아야 합니다. 이때, 가장 큰 정사각형 배열의 면적을 반환합니다.</p>\\n\\n<h3>문제 제한 사항:</h3>\\n<ul>\\n<li>행렬의 크기는 MxN이며, M과 N은 최대 300입니다.</li>\\n</ul>\\n\\n<h3>입력 예시:</h3>\\n<pre><code>4 5\\n1 0 1 0 0\\n1 0 1 1 1\\n1 1 1 1 1\\n1 0 0 1 0\\n</code></pre>\\n\\n<h3>출력 예시:</h3>\\n<pre><code>4\\n</code></pre>\\n\\n<h3>입출력 예 설명:</h3>\\n<p>입력 행렬에서 가장 큰 정사각형의 크기는 2x2이며, 면적은 4입니다.</p>\\n\\n<h3>문제 정보:</h3>\\n<ul>\\n<li>알고리즘: 동적 프로그래밍(DP)</li>\\n<li>난이도: LEVEL3</li>\\n<li>추가 사항: null</li>\\n</ul>\\n\\n<h3>입출력 예 :</h3>\\n<table><tr><th>입력</th><th>출력</th></tr><tr><td>4 5\\n1 0 1 0 0\\n1 0 1 1 1\\n1 1 1 1 1\\n1 0 0 1 0\\n</td><td>4</td></tr><tr><td>3 3\\n0 0 0\\n0 0 0\\n0 0 0\\n</td><td>0</td></tr><tr><td>5 5\\n1 1 0 0 1\\n1 1 0 1 1\\n0 1 1 1 1\\n0 1 1 1 0\\n1 1 1 0 0\\n</td><td>4</td></tr></table>\"")
        private String content;
        @Schema(name = "difficulty", description = "난이도", example = "LEVEL3")
        private Difficulty difficulty;
        @Schema(name = "additionalNotes", description = "추가 사항", example = "문제의 조건을 만족하는 최소의 시간 복잡도를 갖는 알고리즘을 작성해줘.(can be null)")
        private String additionalNotes;
        @Schema(name = "testCases", description = "테스트 케이스 목록(front 필요 없을 확률이 높음.)")
        private List<TestCaseSpec> testCases;

        // Client에서 사용할 응답 객체 생성자
        @Builder
        public CodeGenerateResponse(String title, String algorithm, String content, Difficulty difficulty) {
            this.title = title;
            this.algorithm = algorithm;
            this.content = content;
            this.difficulty = difficulty;
        }

        @Builder
        @Getter
        @AllArgsConstructor
        @NoArgsConstructor
        @Schema(name = "TestCaseSpec", description = "테스트 케이스 명세", title = "TestCaseSpec [테스트 케이스 명세]")
        public static class TestCaseSpec {
            @JsonProperty("input")
            @Schema(name = "input", description = "입력값", example = "5 5\n0 0 0 0 0\n0 -1 0 -1 0\n0 0 0 -1 0\n0 -1 -1 0 -1\n0 0 0 0 0")
            private String input;
            @Schema(name = "output", description = "예상 출력값", example = "7\n")
            @JsonProperty("output")
            private String output;
        }


        public static LLMResponseDTO.CodeGenerateResponse of(String jsonResponse) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                JsonNode rootNode = objectMapper.readTree(jsonResponse);

                JsonNode problemInfoNode = rootNode.path("problem_info").get(0);


                List<TestCaseSpec> contnetTestCases = objectMapper.readValue(
                        rootNode.path("content_test_cases").toString(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, TestCaseSpec.class)
                );

                String content = rootNode.path("content").asText() +"\n\n<h3>입출력 예 :</h3>\n"+ makeContent(contnetTestCases);


                List<TestCaseSpec> testCases = objectMapper.readValue(
                        rootNode.path("test_cases").toString(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, TestCaseSpec.class)
                );

                List<TestCaseSpec> mergedTestCases = Stream.concat(contnetTestCases.stream(), testCases.stream())
                        .toList();

                return LLMResponseDTO.CodeGenerateResponse.builder()
                        .title(rootNode.path("title").asText())
                        .content(content)
                        .algorithm(problemInfoNode.path("algorithm").asText())
                        .difficulty(Difficulty.valueOf(problemInfoNode.path("difficulty").asText()))
                        .additionalNotes(problemInfoNode.path("additional_notes").asText())
                        .testCases(mergedTestCases)
                        .build();
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse JSON response"+ e.getMessage(), e);
            }
        }

        public static String makeContent(List<TestCaseSpec> contentTest){
            return "<table><tr><th>입력</th><th>출력</th></tr>" + contentTest.stream()
                    .map(test -> "<tr><td>"+test.getInput()+"</td><td>"+test.getOutput()+"</td></tr>")
                    .collect(Collectors.joining()) + "</table>";
        }
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(name = "CodeGenerateClientResponse", description = "코딩테스트 생성 클라이언트 응답", title = "CodeGenerateClientResponse [코딩테스트 생성 클라이언트 응답]")
    public static class CodeGenerateClientResponse extends CodeGenerateResponse{
        @Schema(name = "codeId", description = "코딩 테스트 ID", example = "1")
        private Integer codeId;

        public CodeGenerateClientResponse (String title, String algorithm, String content, Difficulty difficulty, Integer codeId) {
            super(title, algorithm, content, difficulty);
            this.codeId = codeId;
        }

        public static CodeGenerateClientResponse of(CodeGenerateResponse codeGenerateResponse, Integer codeId){
            return new CodeGenerateClientResponse(codeGenerateResponse.getTitle(),
                    codeGenerateResponse.getAlgorithm(),
                    codeGenerateResponse.getContent(),
                    codeGenerateResponse.getDifficulty(),
                    codeId);
        }
    }



}