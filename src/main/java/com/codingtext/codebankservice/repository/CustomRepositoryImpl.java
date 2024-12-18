package com.codingtext.codebankservice.repository;

import com.codingtext.codebankservice.Dto.CodeBank.CodeHistoryDto;
import com.codingtext.codebankservice.entity.*;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.NumberExpression;
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
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.Expressions;

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
        QCodeHistory history = QCodeHistory.codeHistory;
        BooleanBuilder builder = new BooleanBuilder();

        // 필터 조건 설정
        if (registerStatus == null) {
            // REGISTERED 또는 PERMITTED 상태를 필터링
            builder.and(code.registerStatus.in(RegisterStatus.REGISTERED));
        } else {
            builder.and(code.registerStatus.eq(registerStatus));
        }

        if (algorithms != null && !algorithms.isEmpty()) {
            List<Algorithm> algorithmEnums = algorithms.stream()
                    .map(algo -> {
                        try {
                            return Algorithm.valueOf(algo.trim().toUpperCase());
                        } catch (IllegalArgumentException e) {
                            return null; // 잘못된 Enum 값은 제외
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            if (!algorithmEnums.isEmpty()) {
                builder.and(code.algorithm.in(algorithmEnums));
            }
        }

        if (difficulties != null && !difficulties.isEmpty()) {
            List<Difficulty> difficultyEnums = difficulties.stream()
                    .map(diff -> {
                        try {
                            return Difficulty.valueOf(diff.trim().toUpperCase());
                        } catch (IllegalArgumentException e) {
                            return null; // 잘못된 Enum 값은 제외
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            if (!difficultyEnums.isEmpty()) {
                builder.and(code.difficulty.in(difficultyEnums));
            }
        }

        if (searchText != null && !searchText.isEmpty()) {
            if ("title".equalsIgnoreCase(searchBy)) {
                builder.and(code.title.containsIgnoreCase(searchText));
            } else if ("codeId".equalsIgnoreCase(searchBy)) {
                builder.and(code.codeId.eq(Long.parseLong(searchText)));
            }
        }

        // 정답률 계산: Boolean 값을 1과 0으로 변환 후 합산
        NumberExpression<Double> correctRate =
                Expressions.numberTemplate(Double.class,
                        "COALESCE(SUM(CASE WHEN {0} = true THEN 1 ELSE 0 END) / NULLIF(COUNT(*), 0), 0)",
                        history.isCorrect);

        // 정렬 조건
        OrderSpecifier<?> orderSpecifier = getOrderSpecifier(sortBy, code, correctRate);

        // 쿼리 실행
        List<Code> results = queryFactory.selectFrom(code)
                .leftJoin(history).on(history.code.codeId.eq(code.codeId))
                .where(builder)
                .groupBy(code.codeId)
                .orderBy(orderSpecifier)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory.select(code.count())
                .from(code)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(results, pageable, total);
    }

    private OrderSpecifier<?> getOrderSpecifier(String sortBy, QCode code, NumberExpression<Double> correctRate) {
        if ("correctRate".equalsIgnoreCase(sortBy)) {
            return correctRate.desc(); // 정답률 내림차순
        } else if ("createdAt".equalsIgnoreCase(sortBy)) {
            return code.createdAt.desc();
        }
        return code.createdAt.asc(); // 기본 정렬
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
