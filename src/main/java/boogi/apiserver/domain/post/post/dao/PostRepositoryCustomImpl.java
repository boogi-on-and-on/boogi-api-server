package boogi.apiserver.domain.post.post.dao;

import boogi.apiserver.domain.hashtag.post.domain.QPostHashtag;
import boogi.apiserver.domain.member.dao.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.QMember;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.post.post.domain.QPost;
import boogi.apiserver.domain.post.post.dto.PostDetail;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
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

import static boogi.apiserver.domain.community.community.domain.QCommunity.*;
import static boogi.apiserver.domain.user.domain.QUser.*;
import static com.querydsl.jpa.JPAExpressions.*;

public class PostRepositoryCustomImpl implements PostRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Autowired
    private MemberRepository memberRepository;

    private final QPost post = QPost.post;
    private final QPostHashtag postHashtag = QPostHashtag.postHashtag;
    private final QMember member = QMember.member;

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
                .join(post.member, member)
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
    public Optional<PostDetail> getPostDetailByPostId(Long postId) {
        Tuple result = queryFactory.select(Projections.constructor(PostDetail.class, post), post.canceledAt, post.deletedAt)
                .from(post)
                .leftJoin(post.member, member).fetchJoin()
                .join(member.user, user).fetchJoin()
                .join(post.community, community).fetchJoin()
                .where(post.id.eq(postId))
                .fetchOne();

        PostDetail postDetail = result.get(0, PostDetail.class);

        if (result.get(1, LocalDateTime.class) == null && result.get(2, LocalDateTime.class) == null) {
            return Optional.ofNullable(postDetail);
        }
        return Optional.empty();
    }
}