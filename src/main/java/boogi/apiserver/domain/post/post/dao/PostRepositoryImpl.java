package boogi.apiserver.domain.post.post.dao;

import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.post.post.dto.dto.SearchPostDto;
import boogi.apiserver.domain.post.post.dto.request.PostQueryRequest;
import boogi.apiserver.global.util.PageableUtil;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static boogi.apiserver.domain.community.community.domain.QCommunity.community;
import static boogi.apiserver.domain.hashtag.post.domain.QPostHashtag.postHashtag;
import static boogi.apiserver.domain.member.domain.QMember.member;
import static boogi.apiserver.domain.post.post.domain.QPost.post;
import static boogi.apiserver.domain.user.domain.QUser.user;


@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Post> getHotPosts() {
        return queryFactory.selectFrom(post)
                .where(
                        post.createdAt.after(LocalDateTime.now().minusDays(4)),
                        post.community.isPrivate.isFalse()
                )
                .join(post.community)
                .orderBy(post.likeCount.desc(), post.commentCount.desc())
                .limit(3)
                .fetch();
    }

    @Override
    public List<Post> getLatestPostOfCommunity(Long communityId) {
        return queryFactory.selectFrom(post)
                .where(post.community.id.eq(communityId))
                .orderBy(post.createdAt.desc())
                .limit(5)
                .fetch();
    }

    @Override
    public Optional<Post> getPostWithAll(Long postId) {
        Post findPost = queryFactory.selectFrom(post)
                .join(post.member, member).fetchJoin()
                .join(member.user, user).fetchJoin()
                .join(post.community, community).fetchJoin()
                .where(post.id.eq(postId))
                .fetchOne();

        return Optional.ofNullable(findPost);
    }

    @Override
    public Slice<Post> getPostsOfCommunity(Pageable pageable, Long communityId) {
        List<Post> posts = queryFactory.selectFrom(post)
                .where(post.community.id.eq(communityId))
                .join(post.member, member).fetchJoin()
                .join(member.user, user).fetchJoin()
                .orderBy(post.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        return PageableUtil.getSlice(posts, pageable);
    }

    @Override
    public Slice<Post> getUserPostPageByMemberIds(List<Long> memberIds, Pageable pageable) {
        List<Post> findPosts = queryFactory.selectFrom(post)
                .where(post.member.id.in(memberIds))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .orderBy(post.createdAt.desc())
                .fetch();

        return PageableUtil.getSlice(findPosts, pageable);
    }

    @Override
    public Slice<SearchPostDto> getSearchedPosts(Pageable pageable, PostQueryRequest request, Long userId) {
        List<Long> memberJoinedCommunityIds = queryFactory.select(member.community.id)
                .from(member)
                .where(
                        member.user.id.eq(userId),
                        member.bannedAt.isNull()
                ).fetch();

        Predicate[] where = {
                post.community.isPrivate.ne(true).or(post.community.id.in(memberJoinedCommunityIds)),
                post.id.in(
                        JPAExpressions.select(postHashtag.post.id)
                                .from(postHashtag)
                                .where(postHashtag.tag.value.eq(request.getKeyword()))
                )
        };

        List<Post> posts = queryFactory.selectFrom(post)
                .where(where)
                .innerJoin(post.community).fetchJoin()
                .innerJoin(post.member, member).fetchJoin()
                .innerJoin(member.user, user).fetchJoin()
                .orderBy(getPostSearchOrder(request))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        List<SearchPostDto> postDtos = posts.stream()
                .map(SearchPostDto::from)
                .collect(Collectors.toList());

        return PageableUtil.getSlice(postDtos, pageable);
    }

    private OrderSpecifier getPostSearchOrder(PostQueryRequest request) {

        switch (request.getOrder()) {
            case NEWER:
                return post.createdAt.desc();
            case OLDER:
                return post.createdAt.asc();
            case LIKE_UPPER:
                return post.likeCount.desc();
        }
        return null;
    }
}