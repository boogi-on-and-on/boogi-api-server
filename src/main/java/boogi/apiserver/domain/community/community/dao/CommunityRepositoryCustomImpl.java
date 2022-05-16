package boogi.apiserver.domain.community.community.dao;

import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.community.community.domain.CommunityCategory;
import boogi.apiserver.domain.community.community.domain.QCommunity;
import boogi.apiserver.domain.community.community.dto.CommunityQueryRequest;
import boogi.apiserver.domain.community.community.dto.SearchCommunityDto;
import boogi.apiserver.domain.community.community.dto.request_enum.CommunityListingOrder;
import boogi.apiserver.domain.hashtag.community.domain.QCommunityHashtag;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.Optional;

public class CommunityRepositoryCustomImpl implements CommunityRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    private final QCommunity community = QCommunity.community;
    private final QCommunityHashtag communityHashtag = QCommunityHashtag.communityHashtag;

    public CommunityRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }


    @Override
    public Page<SearchCommunityDto> getSearchedCommunities(Pageable pageable, CommunityQueryRequest condition) {
        OrderSpecifier order = getOrderSpecifier(condition.getOrder());

        String keyword = condition.getKeyword();

        if (keyword == null) {
            Predicate[] where = {
                    privateEq(condition.getIsPrivate()),
                    categoryEq(condition.getCategory()),
                    community.canceledAt.isNull()
            };

            List<Community> communities = queryFactory.selectFrom(community)
                    .where(where)
                    .orderBy(order)
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize())
                    .fetch();

            // CommunityHashtag LAZY INIT
            communities.stream().anyMatch(c -> c.getHashtags().size() != 0);

            List<SearchCommunityDto> dtos = transformToSearchCommunityDto(communities);

            JPAQuery<Long> countQuery = queryFactory
                    .select(community.count())
                    .from(community)
                    .where(where);

            return PageableExecutionUtils.getPage(dtos, pageable, countQuery::fetchOne);
        }

        QCommunity _community = new QCommunity("communitySub");
        Predicate[] where = {
                privateEq(condition.getIsPrivate()),
                categoryEq(condition.getCategory()),
                community.canceledAt.isNull(),

                //todo: 상관쿼리 작동 원인 찾고 해결
                // 아래 서브쿼리는 독립적으로 작동할 수 있는 비상관쿼리
                // 그런데, or query로 하면 상관 서브쿼리로 작동..
                // and로 하면 비상관쿼리로 작동한다.

                community.communityName.contains(condition.getKeyword()).or(
                        community.id.in(JPAExpressions.select(_community.id)
                                .from(_community)
                                .where(communityHashtag.tag.eq(condition.getKeyword()))
                                .innerJoin(_community.hashtags, communityHashtag))
                )};

        List<Community> communities = queryFactory.selectFrom(community)
                .where(where)
                .orderBy(order)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        //LAZY INIT
        communities.stream().anyMatch(c -> c.getHashtags().size() != 0);

        List<SearchCommunityDto> dtos = transformToSearchCommunityDto(communities);

        JPAQuery<Long> countQuery = queryFactory
                .select(community.count())
                .from(community)
                .where(where)
                .leftJoin(community.hashtags);

        return PageableExecutionUtils.getPage(dtos, pageable, countQuery::fetchOne);
    }

    private OrderSpecifier getOrderSpecifier(CommunityListingOrder condition) {
        OrderSpecifier order = community.createdAt.desc();
        switch (condition) {
            case NEWER:
                order = community.createdAt.desc();
                break;
            case OLDER:
                order = community.createdAt.asc();
                break;
            case MANY_PEOPLE:
                order = community.memberCount.desc();
            case LESS_PEOPLE:
                order = community.memberCount.asc();
            default:
                order = community.createdAt.desc();

        }
        return order;
    }

    private BooleanExpression privateEq(Boolean isPrivate) {
        return isPrivate ? community.isPrivate.eq(true) : community.isPrivate.ne(true);
    }

    private BooleanExpression categoryEq(CommunityCategory category) {
        return Objects.isNull(category) ? null : community.category.eq(category);
    }

    private BooleanExpression communityNameContains(String keyword) {
        return Objects.isNull(keyword) ? null : community.communityName.contains(keyword);
    }

    private List<SearchCommunityDto> transformToSearchCommunityDto(List<Community> communities) {
        return communities.stream()
                .map(SearchCommunityDto::of)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Community> findCommunityById(Long communityId) {
        Community findCommunity = queryFactory.selectFrom(QCommunity.community)
                .where(
                        QCommunity.community.id.eq(communityId),
                        QCommunity.community.deletedAt.isNull(),
                        QCommunity.community.canceledAt.isNull()
                ).fetchOne();

        return Optional.ofNullable(findCommunity);
    }
}
