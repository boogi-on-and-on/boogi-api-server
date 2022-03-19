package boogi.apiserver.domain.post.post.dao;

import boogi.apiserver.domain.hashtag.post.domain.QPostHashtag;
import boogi.apiserver.domain.member.dao.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.QMember;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.post.post.domain.QPost;
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
import java.util.stream.Collectors;

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
    public List<Post> getLatestPostOfCommunity(Long userId) {
        List<Long> memberJoinedCommunityIds = memberRepository.findByUserId(userId)
                .stream()
                .map(m -> m.getCommunity().getId())
                .collect(Collectors.toList());

        QPost _post = new QPost("sub_post");

        List<Post> posts = queryFactory.selectFrom(post)
                .where(
                        post.id.in(JPAExpressions
                                .select(_post.id.max()) //가장 최근일 수록 id가 최대인 점을 이용
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
}
