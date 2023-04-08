package boogi.apiserver.domain.community.community.dao;

import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.community.community.domain.CommunityCategory;
import boogi.apiserver.domain.community.community.dto.dto.SearchCommunityDto;
import boogi.apiserver.domain.community.community.dto.enums.CommunityListingOrder;
import boogi.apiserver.domain.community.community.dto.request.CommunityQueryRequest;
import boogi.apiserver.global.util.PageableUtil;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;
import java.util.Objects;

import static boogi.apiserver.domain.community.community.domain.QCommunity.community;
import static boogi.apiserver.domain.hashtag.community.domain.QCommunityHashtag.communityHashtag;


@RequiredArgsConstructor
public class CommunityRepositoryImpl implements CommunityRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Slice<SearchCommunityDto> getSearchedCommunities(Pageable pageable, CommunityQueryRequest condition) {
        OrderSpecifier order = getOrderSpecifier(condition.getOrder());
        Predicate[] where = getWherePredicates(condition, condition.getKeyword());

        List<Community> communities = queryFactory.selectFrom(community)
                .where(where)
                .orderBy(order)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        List<SearchCommunityDto> dtos = SearchCommunityDto.listOf(communities);

        return PageableUtil.getSlice(dtos, pageable);
    }

    private Predicate[] getWherePredicates(CommunityQueryRequest condition, String keyword) {
        if (keyword == null) {
            return new Predicate[]{
                    privateEq(condition.getIsPrivate()),
                    categoryEq(condition.getCategory())
            };
        }
        return new Predicate[]{
                privateEq(condition.getIsPrivate()),
                categoryEq(condition.getCategory()),

                community.communityName.value.contains(keyword).or(
                        community.id.in(JPAExpressions.select(communityHashtag.community.id)
                                .from(communityHashtag)
                                .where(communityHashtag.tag.value.eq(keyword)))
                )
        };
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
                break;
            case LESS_PEOPLE:
                order = community.memberCount.asc();
                break;
            default:
                order = community.createdAt.desc();
        }
        return order;
    }

    private BooleanExpression privateEq(Boolean isPrivate) {
        if (Objects.isNull(isPrivate)) {
            return null;
        }
        return isPrivate ? community.isPrivate.eq(true) : community.isPrivate.ne(true);
    }

    private BooleanExpression categoryEq(CommunityCategory category) {
        return Objects.isNull(category) ? null : community.category.eq(category);
    }
}
