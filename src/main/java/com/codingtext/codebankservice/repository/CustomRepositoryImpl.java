package com.codingtext.codebankservice.repository;

import com.codingtext.codebankservice.Dto.CodeDto;
import com.codingtext.codebankservice.Service.CodeService;
import com.codingtext.codebankservice.entity.Code;
import com.codingtext.codebankservice.entity.QCode;
import com.codingtext.codebankservice.entity.Algorithm;
import com.codingtext.codebankservice.entity.Difficulty;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.AllArgsConstructor;
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
       // OrderSpecifier<?> orderSpecifier = getOrderSpecifier(sortBy, code);

        // 쿼리 실행 및 결과 가져오기
//        List<Code> results = queryFactory.selectFrom(code)
//                .where(builder)
//                //.orderBy(orderSpecifier)
//                .offset(pageable.getOffset())
//                .limit(pageable.getPageSize())
//                .fetch();
//
//        long total = queryFactory.select(code.count())
//                .from(code)
//                .where(builder)
//                .fetchOne();
//
//        return new PageImpl<>(results, pageable, total);
        List<Code> results = queryFactory.selectFrom(code)
                .where(builder)
                .orderBy(code.createdAt.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory.selectFrom(code)
                .where(builder)
                .fetchCount();

        return new PageImpl<>(results, pageable, total);
    }



}
