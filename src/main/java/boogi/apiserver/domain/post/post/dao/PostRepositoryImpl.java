package boogi.apiserver.domain.post.post.dao;

import boogi.apiserver.domain.community.community.domain.QCommunity;
import boogi.apiserver.domain.hashtag.post.domain.QPostHashtag;
import boogi.apiserver.domain.member.dao.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.QMember;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.post.post.domain.QPost;
import boogi.apiserver.domain.post.post.dto.request.PostQueryRequest;
import boogi.apiserver.domain.post.post.dto.response.SearchPostDto;
import boogi.apiserver.domain.user.domain.QUser;
import boogi.apiserver.global.util.PageableUtil;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Autowired
    private MemberRepository memberRepository;

    private final QPost post = QPost.post;
    private final QPostHashtag postHashtag = QPostHashtag.postHashtag;
    private final QMember member = QMember.member;
    private final QUser user = QUser.user;
    private final QCommunity community = QCommunity.community;

    @Override
    public Slice<Post> getUserPostPage(Pageable pageable, Long userId) {
        List<Long> memberIds = memberRepository.findByUserId(userId)
                .stream()
                .map(Member::getId)
                .collect(Collectors.toList());

        // TODO: 자신의 프로필 조회한 경우?
        List<Post> posts =
                queryFactory
                        .selectFrom(post)
                        .where(
                                post.member.id.in(memberIds),
                                post.deletedAt.isNull()
                        )
                        .join(post.community)
                        .fetchJoin()
                        .orderBy(post.createdAt.desc())
                        .offset(pageable.getOffset())
                        .limit(pageable.getPageSize() + 1)
                        .fetch();

        // LAZY INIT PostHashtag
        posts.stream().anyMatch(p -> p.getHashtags().size() != 0);

        //LAZY INIT PostMedia
        posts.stream().anyMatch(p -> p.getPostMedias().size() > 0);

        return PageableUtil.getSlice(posts, pageable);
    }

    @Override
    public List<Post> getHotPosts() {
        return queryFactory.selectFrom(post)
                .where(
                        post.createdAt.after(LocalDateTime.now().minusDays(4)),
                        post.deletedAt.isNull(),
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
                .where(
                        post.community.id.eq(communityId),
                        post.deletedAt.isNull()
                )
                .orderBy(post.createdAt.desc())
                .limit(5)
                .fetch();
    }

    @Override
    public Optional<Post> getPostWithUserAndMemberAndCommunityByPostId(Long postId) {
        Post findPost = queryFactory.selectFrom(this.post)
                .join(this.post.member, member).fetchJoin()
                .join(member.user, user).fetchJoin()
                .join(this.post.community, community).fetchJoin()
                .where(
                        this.post.id.eq(postId),
                        this.post.deletedAt.isNull(),
                        this.post.community.deletedAt.isNull()
                ).fetchOne();

        return Optional.ofNullable(findPost);
    }

    @Override
    public Slice<Post> getPostsOfCommunity(Pageable pageable, Long communityId) {
        List<Post> posts = queryFactory.selectFrom(post)
                .where(
                        post.community.id.eq(communityId),
                        post.deletedAt.isNull()
                )
                .join(post.member, member).fetchJoin()
                .join(member.user, user).fetchJoin()
                .orderBy(post.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        // LAZY INIT PostHashtag
        posts.stream().anyMatch(p -> p.getHashtags() != null && p.getHashtags().size() > 0);

        // LAZY INIT PostMedia
        posts.stream().anyMatch(p -> p.getPostMedias().size() > 0);

        //LAZY INIT Like
        //todo: MemberId 기반으로 쿼리하기
        posts.stream().anyMatch(p -> p.getLikes().size() > 0);

        return PageableUtil.getSlice(posts, pageable);
    }

    @Override
    public Optional<Post> getPostWithCommunityAndMemberByPostId(Long postId) {
        Post result = queryFactory.selectFrom(this.post)
                .join(this.post.community, community).fetchJoin()
                .join(this.post.member, member).fetchJoin()
                .where(
                        this.post.id.eq(postId),
                        this.post.deletedAt.isNull())
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public Slice<SearchPostDto> getSearchedPosts(Pageable pageable, PostQueryRequest request, Long userId) {
        List<Long> memberJoinedCommunityIds = queryFactory.select(member.community.id)
                .from(member)
                .where(member.user.id.eq(userId),
                        member.bannedAt.isNull(),
                        member.createdAt.isNull()
                ).fetch();

        QPost _post = new QPost("postSub");
        Predicate[] where = {
                post.community.isPrivate.ne(true).or(post.community.id.in(memberJoinedCommunityIds)),
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
                .limit(pageable.getPageSize() + 1)
                .fetch();

        //PostHashTag LAZY INIT
        posts.stream().anyMatch(p -> p.getHashtags().size() > 0);

        //PostMedia LAZY INIT
        posts.stream().anyMatch(p -> p.getPostMedias().size() > 0);

        List<SearchPostDto> postDtos = posts.stream()
                .map(SearchPostDto::new)
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

    @Override
    public Optional<Post> findPostById(Long postId) {
        Post findPost = queryFactory.selectFrom(this.post)
                .where(
                        this.post.id.eq(postId),
                        this.post.deletedAt.isNull()
                ).fetchOne();

        return Optional.ofNullable(findPost);
    }

    @Override
    public Slice<Post> getUserPostPageByMemberIds(List<Long> memberIds, Pageable pageable) {
        List<Post> findPosts = queryFactory.selectFrom(post)
                .where(
                        post.member.id.in(memberIds),
                        post.deletedAt.isNull()
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .orderBy(post.createdAt.desc())
                .fetch();

        return PageableUtil.getSlice(findPosts, pageable);
    }
}