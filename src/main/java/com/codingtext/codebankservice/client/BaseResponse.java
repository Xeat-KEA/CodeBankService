package com.codingtext.codebankservice.client;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BaseResponse<T> {
    private Integer statusCode;
    private String message;
    private T data;

    // 성공 응답 생성
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(200, "요청 성공", data);
    }

    // 성공 응답 생성
    public static <T> BaseResponse<T> error(Integer statusCode, String message, T data) {
        return new BaseResponse<>(statusCode, message, data);
    }
}