//package com.codingtext.codebankservice.Util;
//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.SignatureAlgorithm;
//import io.jsonwebtoken.security.Keys;
//
//import java.security.Key;
//import java.util.Date;
//
//public class JwtUtil {
//
//    // 비밀 키 (서명 검증용, 실제 환경에서는 환경 변수나 안전한 저장소에 저장)
//    // 뭔지 잘모르겠는데 숨겨야함
//    private static final Key SECRET_KEY //= Keys.secretKeyFor(SignatureAlgorithm.HS256);
//
//    // JWT 파싱 및 검증
//    public static Claims parseToken(String token) {
//        return Jwts.parserBuilder()
//                .setSigningKey(SECRET_KEY) // 서명 검증
//                .build()
//                .parseClaimsJws(token) // 토큰 파싱
//                .getBody(); // Payload 반환
//    }
//
//    // 토큰 유효성 검증
//    public static boolean isTokenValid(String token) {
//        try {
//            parseToken(token); // 파싱 중 에러가 없으면 유효
//            return true;
//        } catch (Exception e) {
//            return false; // 서명 불일치, 만료 등으로 검증 실패
//        }
//    }
//
//    // 토큰에서 사용자 ID 추출
//    public static String getUserIdFromToken(String token) {
//        Claims claims = parseToken(token);
//        return claims.getSubject(); // JWT의 'sub' 클레임이 사용자 ID
//    }
//}
