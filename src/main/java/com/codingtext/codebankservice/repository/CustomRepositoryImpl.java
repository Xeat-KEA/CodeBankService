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
import java.util.stream.Collectors;

@Repository
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
                                                   Pageable pageable) {
        QCode code = QCode.code;
        BooleanBuilder builder = new BooleanBuilder();

        // 알고리즘 필터 추가
        if (algorithms != null && !algorithms.isEmpty()) {
            builder.and(code.algorithm.in(algorithms.stream()
                    .map(Algorithm::valueOf)
                    .collect(Collectors.toList())));
        }

        // 난이도 필터 추가
        if (difficulties != null && !difficulties.isEmpty()) {
            builder.and(code.difficulty.in(difficulties.stream()
                    .map(Difficulty::valueOf)
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
        BooleanBuilder builder = new BooleanBuilder();


        // 사용자 필터
        builder.and(history.userId.eq(userId));

        // 알고리즘 필터
        if (algorithms != null && !algorithms.isEmpty()) {
            builder.and(history.code.algorithm.in(algorithms.stream()
                    .map(Algorithm::valueOf)
                    .collect(Collectors.toList())));
        }

        // 난이도 필터
        if (difficulties != null && !difficulties.isEmpty()) {
            builder.and(history.code.difficulty.in(difficulties.stream()
                    .map(Difficulty::valueOf)
                    .collect(Collectors.toList())));
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
        OrderSpecifier<?> orderSpecifier = getOrderSpecifier(history);

        // 쿼리 실행
        List<CodeHistory> results = queryFactory.selectFrom(history)
                .where(builder)
                .orderBy(orderSpecifier)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory.select(history.count())
                .from(history)
                .where(builder)
                .fetchOne();
        // `CodeHistory`를 `CodeHistoryDto`로 변환
        List<CodeHistoryDto> dtoResults = results.stream()
                .map(CodeHistoryDto::ToDto)
                .collect(Collectors.toList());

        return new PageImpl<>(dtoResults, pageable, total);
    }

    private OrderSpecifier<?> getOrderSpecifier(QCodeHistory history) {

            // 히스토리 생성 기준 정렬
            return history.createdAt.desc();


    }




}
