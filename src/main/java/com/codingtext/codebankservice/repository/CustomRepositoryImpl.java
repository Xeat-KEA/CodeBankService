package com.codingtext.codebankservice.repository;

import com.codingtext.codebankservice.entity.Code;
import com.codingtext.codebankservice.entity.QCode;
import com.codingtext.codebankservice.entity.Algorithm;
import com.codingtext.codebankservice.entity.Difficulty;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import jakarta.persistence.EntityManager;
import java.util.List;

@Repository
public class CustomRepositoryImpl implements CustomRepository {

    private final JPAQueryFactory queryFactory;

    public CustomRepositoryImpl(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    @Override
    public Page<Code> findCodesByAlgorithmAndDifficulty(String algorithm, String difficulty, Pageable pageable) {
        QCode code = QCode.code;
        BooleanBuilder builder = new BooleanBuilder();

        // 알고리즘 필터 추가
        if (algorithm != null && !algorithm.isEmpty()) {
            builder.and(code.algorithm.eq(Algorithm.valueOf(algorithm.toUpperCase())));
        }

        // 난이도 필터 추가
        if (difficulty != null && !difficulty.isEmpty()) {
            builder.and(code.difficulty.eq(Difficulty.valueOf(difficulty.toUpperCase())));
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