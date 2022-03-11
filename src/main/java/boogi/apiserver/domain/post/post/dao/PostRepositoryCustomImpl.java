package boogi.apiserver.domain.post.post.dao;

import boogi.apiserver.domain.hashtag.post.domain.QPostHashtag;
import boogi.apiserver.domain.member.dao.MemberRepository;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.post.post.domain.QPost;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import javax.persistence.EntityManager;
import java.util.List;

public class PostRepositoryCustomImpl implements PostRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Autowired
    private MemberRepository memberRepository;

    private final QPost post = QPost.post;
    private final QPostHashtag postHashtag = QPostHashtag.postHashtag;

    public PostRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Page<Post> getUserPostPage(Pageable pageable, Long userId) {
        List<Long> memberIds = memberRepository.findMemberIdsByUserId(userId);

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
}