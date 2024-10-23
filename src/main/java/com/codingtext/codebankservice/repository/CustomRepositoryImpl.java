package com.codingtext.codebankservice.repository;

import com.codingtext.codebankservice.Dto.CodeDto;
import com.codingtext.codebankservice.entity.Code;
import com.codingtext.codebankservice.entity.QCode;
import com.codingtext.codebankservice.entity.Algorithm;
import com.codingtext.codebankservice.entity.Difficulty;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
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
                    .map(algorithm -> Algorithm.valueOf(algorithm.toUpperCase()))  // String을 Enum으로 변환
                    .collect(Collectors.toList())));
        }

        // 난이도 필터 추가
        if (difficulties != null && !difficulties.isEmpty()) {
            builder.and(code.difficulty.in(difficulties.stream()
                    .map(difficulty -> Difficulty.valueOf(difficulty.toUpperCase()))  // String을 Enum으로 변환
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
                    // 숫자로 변환할 수 없는 경우, 검색 결과 없음 처리
                    builder.and(code.codeId.isNull());
                }
            }
        }
        List<Code> results = queryFactory.selectFrom(code)
                .where(builder)
                .orderBy(code.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory.selectFrom(code)
                .where(builder)
                .fetchCount();




        return new PageImpl<>(results, pageable, total);
    }
}