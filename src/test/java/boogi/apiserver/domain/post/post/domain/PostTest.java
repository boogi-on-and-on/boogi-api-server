package boogi.apiserver.domain.post.post.domain;

import boogi.apiserver.builder.TestMember;
import boogi.apiserver.builder.TestPost;
import boogi.apiserver.builder.TestPostMedia;
import boogi.apiserver.builder.TestUser;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.post.postmedia.domain.PostMedia;
import boogi.apiserver.domain.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


class PostTest {

    @Test
    @DisplayName("태그 추가에 성공한다.")
    void addTagsSuccess() {
        Post post = TestPost.builder().build();

        post.addTags(List.of("태그1", "태그2"));

        assertThat(post.getHashtags()).extracting("tag").containsExactlyInAnyOrder("태그1", "태그2");
    }

    @Test
    @DisplayName("게시글 미디어 추가에 성공한다.")
    void addPostMediasSuccess() {
        Post post = TestPost.builder().build();

        PostMedia media1 = TestPostMedia.builder().uuid("uuid1").build();
        PostMedia media2 = TestPostMedia.builder().uuid("uuid2").build();

        post.addPostMedias(List.of(media1, media2));

        assertThat(post.getPostMedias()).extracting("post").containsExactlyInAnyOrder(post, post);
        assertThat(post.getPostMedias()).extracting("uuid").containsExactlyInAnyOrder("uuid1", "uuid2");
    }

    @Test
    @DisplayName("게시글 수정에 성공한다.")
    void updatePostSuccess() {
        final String UPDATE_CONTENT = "변경할 게시글 내용입니다.";

        Post post = TestPost.builder()
                .content("게시글 내용입니다.")
                .hashtags(new ArrayList<>())
                .postMedias(new ArrayList<>())
                .build();

        PostMedia media1 = TestPostMedia.builder().uuid("uuid1").build();
        PostMedia media2 = TestPostMedia.builder().uuid("uuid2").build();

        post.updatePost(UPDATE_CONTENT, List.of("태그1", "태그2"), List.of(media1, media2));

        assertThat(post.getContent()).isEqualTo(UPDATE_CONTENT);
        assertThat(post.getHashtags()).extracting("tag").containsExactlyInAnyOrder("태그1", "태그2");
        assertThat(post.getPostMedias()).extracting("uuid").containsExactlyInAnyOrder("uuid1", "uuid2");
    }

    @Test
    @DisplayName("좋아요 수가 0일때는 좋아요 수를 감소시키지 않는다.")
    void removeLikeCountNotUnderZeroSuccess() {
        Post post = TestPost.builder().likeCount(0).build();

        post.removeLikeCount();

        assertThat(post.getLikeCount()).isZero();
    }

    @Test
    @DisplayName("댓글 수가 0일때는 댓글 수를 감소시키지 않는다.")
    void removeCommentCountNotUnderZeroSuccess() {
        Post post = TestPost.builder().commentCount(0).build();

        post.removeCommentCount();

        assertThat(post.getCommentCount()).isZero();
    }

    @Nested
    @DisplayName("게시글 작성자 판별")
    class IsAuthor {
        private static final long AUTHOR_ID = 1l;
        private static final long NOT_AUTHOR_ID = 2l;

        @Test
        @DisplayName("게시글 작성 유저와 ID가 같으면 true를 반환한다.")
        void authorUserSuccess() {
            User user = TestUser.builder().id(AUTHOR_ID).build();
            Member member = TestMember.builder().user(user).build();

            Post post = TestPost.builder().member(member).build();

            assertThat(post.isAuthor(AUTHOR_ID)).isTrue();
        }

        @Test
        @DisplayName("게시글 작성 유저와 ID가 다르면 false를 반환한다.")
        void notAuthorUserSuccess() {
            User user = TestUser.builder().id(AUTHOR_ID).build();
            Member member = TestMember.builder().user(user).build();

            Post post = TestPost.builder().member(member).build();

            assertThat(post.isAuthor(NOT_AUTHOR_ID)).isFalse();
        }
    }
}