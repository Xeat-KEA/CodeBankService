package com.codingtext.codebankservice.repository;

import com.codingtext.codebankservice.Dto.CodeBank.CodeHistoryDto;
import com.codingtext.codebankservice.entity.*;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Repository("customRepository")
public class CustomRepositoryImpl implements CustomRepository {
    private final JPAQueryFactory queryFactory;

    public CustomRepositoryImpl(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }
    @Override
    public Page<Code> findCodesWithFilterAndSearch(List<String> algorithms,
                                                   List<String> difficulties,
                                                   String searchBy,
                                                   String searchText,
                                                   String sortBy,
                                                   RegisterStatus registerStatus,
                                                   Pageable pageable) {
        QCode code = QCode.code;
        BooleanBuilder builder = new BooleanBuilder();

        // 알고리즘 필터 추가
//        if (algorithms != null && !algorithms.isEmpty()) {
//            builder.and(code.algorithm.in(algorithms.stream()
//                    .map(Algorithm::valueOf)
//                    .collect(Collectors.toList())));
//        }

        if (algorithms != null && !algorithms.isEmpty()) {
            builder.and(code.algorithm.in(algorithms.stream()
                    .map(algo -> {
                        try {
                            return Algorithm.valueOf(algo.trim().toUpperCase());
                        } catch (IllegalArgumentException e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList())));
        }

        // 난이도 필터 추가
//        if (difficulties != null && !difficulties.isEmpty()) {
//            builder.and(code.difficulty.in(difficulties.stream()
//                    .map(Difficulty::valueOf)
//                    .collect(Collectors.toList())));
//        }
        if (difficulties != null && !difficulties.isEmpty()) {
            builder.and(code.difficulty.in(difficulties.stream()
                    .map(diff -> {
                        try {
                            return Difficulty.valueOf(diff.trim().toUpperCase());
                        } catch (IllegalArgumentException e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList())));
        }

        // 검색어 처리
        if (searchText != null && !searchText.isEmpty()) {
            if ("title".equalsIgnoreCase(searchBy)) {
                builder.and(code.title.containsIgnoreCase(searchText));
            } else if ("codeId".equalsIgnoreCase(searchBy)) {
                try {
                    Long codeId = Long.parseLong(searchText);
                    builder.and(code.codeId.eq(codeId));
                } catch (NumberFormatException e) {
                    builder.and(code.codeId.isNull());
                }
            }
        }
        System.out.println("Filters: " + builder.getValue());
        System.out.println("Offset: " + pageable.getOffset());
        System.out.println("Page size: " + pageable.getPageSize());

        // 동적 정렬 조건 생성
        OrderSpecifier<?> orderSpecifier = getOrderSpecifier(sortBy, code);

        List<Code> results = queryFactory.selectFrom(code)
                .where(builder)
                .orderBy(orderSpecifier)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory.select(code.count())
                .from(code)
                .where(builder)
                .fetchOne();
        System.out.println("Results: " + results);
        System.out.println("Total count: " + total);

        return new PageImpl<>(results, pageable, total);
    }
    private OrderSpecifier<?> getOrderSpecifier(String sortBy, QCode code) {
        if ("createdAt".equalsIgnoreCase(sortBy)) {
            // sortBy가 createdAt인 경우 최신 등록된 문제 먼저
            return code.createdAt.desc();
        } else {
            // 기본 정렬 조건: 가장 먼저 생성된 문제가 먼저
            return code.createdAt.asc();
        }
    }
    @Override
    public Page<CodeHistoryDto> findUserHistoriesWithFilterAndSearch(String userId,
                                                                     List<String> algorithms,
                                                                     List<String> difficulties,
                                                                     String searchBy,
                                                                     String searchText,
                                                                     Pageable pageable) {
        QCodeHistory history = QCodeHistory.codeHistory;
        QCode code = QCode.code; // 조인을 명시적으로 선언
        BooleanBuilder builder = new BooleanBuilder();

        // 사용자 필터
        builder.and(history.userId.eq(userId));

        // 알고리즘 필터
        if (algorithms != null && !algorithms.isEmpty()) {
            List<Algorithm> algorithmEnums = algorithms.stream()
                    .map(algo -> {
                        try {
                            return Algorithm.valueOf(algo.toUpperCase()); // 대소문자 무시
                        } catch (IllegalArgumentException e) {
                            return null; // 잘못된 값은 무시
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            if (!algorithmEnums.isEmpty()) {
                builder.and(history.code.algorithm.in(algorithmEnums));
            }
        }

        // 난이도 필터
        if (difficulties != null && !difficulties.isEmpty()) {
            List<Difficulty> difficultyEnums = difficulties.stream()
                    .map(diff -> {
                        try {
                            return Difficulty.valueOf(diff.toUpperCase()); // 대소문자 무시
                        } catch (IllegalArgumentException e) {
                            return null; // 잘못된 값은 무시
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            if (!difficultyEnums.isEmpty()) {
                builder.and(history.code.difficulty.in(difficultyEnums));
            }
        }

        // 검색어 처리
        if (searchText != null && !searchText.isEmpty()) {
            if ("title".equalsIgnoreCase(searchBy)) {
                builder.and(history.code.title.containsIgnoreCase(searchText));
            } else if ("codeId".equalsIgnoreCase(searchBy)) {
                try {
                    Long codeId = Long.parseLong(searchText);
                    builder.and(history.code.codeId.eq(codeId));
                } catch (NumberFormatException e) {
                    builder.and(history.code.codeId.isNull());
                }
            }
        }

        // 동적 정렬 조건
        OrderSpecifier<?> orderSpecifier = getOrderSpecifierh(history);

        // 쿼리 실행
        List<CodeHistory> results = queryFactory.selectFrom(history)
                .leftJoin(history.code, code) // 명시적 조인
                .fetchJoin() // 성능 최적화
                .where(builder)
                .orderBy(orderSpecifier)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory.select(history.count())
                .from(history)
                .leftJoin(history.code, code) // 동일하게 조인
                .where(builder)
                .fetchOne();

        // `CodeHistory`를 `CodeHistoryDto`로 변환
        List<CodeHistoryDto> dtoResults = results.stream()
                .map(CodeHistoryDto::ToDto)
                .collect(Collectors.toList());

        return new PageImpl<>(dtoResults, pageable, total);
    }
    // QCodeHistory에 대한 정렬
    private OrderSpecifier<?> getOrderSpecifierh(QCodeHistory history) {
        // 기본적으로 생성일 내림차순 정렬
        return history.createdAt.desc();
    }





}
