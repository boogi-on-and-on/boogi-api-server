package boogi.apiserver.domain.post.post.dao;

import boogi.apiserver.domain.community.community.domain.QCommunity;
import boogi.apiserver.domain.hashtag.post.domain.QPostHashtag;
import boogi.apiserver.domain.member.dao.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.QMember;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.post.post.domain.QPost;
import boogi.apiserver.domain.post.post.dto.PostDetail;
import boogi.apiserver.domain.post.post.dto.PostQueryRequest;
import boogi.apiserver.domain.post.post.dto.SearchPostDto;
import boogi.apiserver.domain.user.domain.QUser;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.querydsl.jpa.JPAExpressions.select;

public class PostRepositoryCustomImpl implements PostRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Autowired
    private MemberRepository memberRepository;

    private final QPost post = QPost.post;
    private final QPostHashtag postHashtag = QPostHashtag.postHashtag;
    private final QMember member = QMember.member;
    private final QUser user = QUser.user;
    private final QCommunity community = QCommunity.community;

    public PostRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Page<Post> getUserPostPage(Pageable pageable, Long userId) {
        List<Long> memberIds = memberRepository.findByUserId(userId)
                .stream()
                .map(Member::getId)
                .collect(Collectors.toList());

        // TODO: 자신의 프로필 조회한 경우?
        // TODO: DTO로 변환하기
        List<Post> posts =
                queryFactory
                        .selectFrom(post)
                        .where(post.member.id.in(memberIds))
                        .join(post.community)
                        .fetchJoin()
                        .orderBy(post.createdAt.desc())
                        .offset(pageable.getOffset())
                        .limit(pageable.getPageSize())
                        .fetch();

        posts.stream().anyMatch(p -> p.getHashtags().size() != 0); // LAZY INIT

        JPAQuery<Post> countQuery = queryFactory.selectFrom(post).where(post.member.id.in(memberIds));

        return PageableExecutionUtils.getPage(posts, pageable, countQuery::fetchCount);
    }

    @Override
    public List<Post> getHotPosts() {
        return queryFactory.selectFrom(post)
                .where(
                        post.createdAt.after(LocalDateTime.now().minusDays(4)),
                        post.canceledAt.isNull(),
                        post.community.isPrivate.isFalse()
                )
                .join(post.community)
                .orderBy(post.likeCount.desc(), post.commentCount.desc())
                .limit(3)
                .fetch();
    }

    @Override
    public List<Post> getLatestPostOfUserJoinedCommunities(Long userId) {
        List<Long> memberJoinedCommunityIds = memberRepository.findByUserId(userId)
                .stream()
                .map(m -> m.getCommunity().getId())
                .collect(Collectors.toList());

        QPost _post = new QPost("sub_post");

        List<Post> posts = queryFactory.selectFrom(post)
                .where(
                        post.id.in(select(_post.id.max()) //가장 최근일 수록 id가 최대인 점을 이용
                                .from(_post)
                                .where(
                                        _post.community.id.in(memberJoinedCommunityIds))
                                .groupBy(_post.community.id)))
                .innerJoin(post.member, member)
                .orderBy(post.member.createdAt.asc())
                .fetch();

        //LAZY INIT
        posts.stream().map(p -> p.getHashtags().size() > 0).findFirst();

        return posts;
    }

    @Override
    public List<Post> getLatestPostOfCommunity(Long communityId) {
        return queryFactory.selectFrom(post)
                .where(
                        post.community.id.eq(communityId),
                        post.canceledAt.isNull()
                )
                .orderBy(post.createdAt.desc())
                .limit(5)
                .fetch();
    }

    //TODO: member, user canceledAt, deletedAt validation 추가
    @Override
    public Optional<Post> getPostWithUserAndMemberAndCommunityByPostId(Long postId) {
        Post findPost = queryFactory.selectFrom(this.post)
                .join(this.post.member, member).fetchJoin()
                .join(member.user, user).fetchJoin()
                .join(this.post.community, community).fetchJoin()
                .where(
                        this.post.id.eq(postId),
                        this.post.canceledAt.isNull(),
                        this.post.deletedAt.isNull(),
                        this.post.community.deletedAt.isNull(),
                        this.post.community.canceledAt.isNull()
                ).fetchOne();

        return Optional.ofNullable(findPost);
    }

    @Override
    public Page<Post> getPostsOfCommunity(Pageable pageable, Long communityId) {
        List<Post> posts = queryFactory.selectFrom(post)
                .where(
                        post.community.id.eq(communityId),
                        post.canceledAt.isNull(),
                        post.deletedAt.isNull()
                )
                .join(post.member, member).fetchJoin()
                .join(member.user, user).fetchJoin()
                .orderBy(post.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        //LAZY INIT
        posts.stream().map(p -> p.getHashtags() != null && p.getHashtags().size() > 0).findFirst();

        JPAQuery<Long> countQuery = queryFactory.select(this.post.count())
                .from(this.post)
                .where(
                        this.post.community.id.eq(communityId),
                        this.post.canceledAt.isNull(),
                        this.post.deletedAt.isNull());

        return PageableExecutionUtils.getPage(posts, pageable, countQuery::fetchOne);
    }

    @Override
    public Optional<Post> getPostWithCommunityAndMemberByPostId(Long postId) {
        Post result = queryFactory.selectFrom(this.post)
                .join(this.post.community, community).fetchJoin()
                .join(this.post.member, member).fetchJoin()
                .where(
                        this.post.id.eq(postId),
                        this.post.canceledAt.isNull(),
                        this.post.deletedAt.isNull())
                .fetchOne();

        return Optional.ofNullable(result);
    }

    public Page<SearchPostDto> getSearchedPosts(Pageable pageable, PostQueryRequest request, Long userId) {
        List<Long> memberIds = queryFactory.select(member.id)
                .from(member)
                .where(member.user.id.eq(userId),
                        member.bannedAt.isNull(),
                        member.createdAt.isNull()
                ).fetch();

        QPost _post = new QPost("postSub");
        Predicate[] where = {
                post.community.isPrivate.ne(true).or(post.member.id.in(memberIds)),
                post.community.deletedAt.isNull(),
                post.deletedAt.isNull(),
                post.id.in(
                        JPAExpressions.select(_post.id)
                                .from(_post)
                                .where(postHashtag.tag.eq(request.getKeyword()))
                                .innerJoin(_post.hashtags, postHashtag)
                )
        };

        List<Post> posts = queryFactory.selectFrom(post)
                .where(where)
                .innerJoin(post.community).fetchJoin()
                .innerJoin(post.member, member).fetchJoin()
                .innerJoin(member.user, user).fetchJoin()
                .orderBy(getPostSearchOrder(request))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        //PostHashTag LAZY INIT
        posts.stream().anyMatch(p -> p.getHashtags().size() > 0);

        List<SearchPostDto> postDtos = posts.stream()
                .map(SearchPostDto::new)
                .collect(Collectors.toList());

        JPAQuery<Long> countQuery = queryFactory.select(post.count())
                .from(post)
                .where(where)
                .innerJoin(post.community);

        return PageableExecutionUtils.getPage(postDtos, pageable, countQuery::fetchOne);
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
