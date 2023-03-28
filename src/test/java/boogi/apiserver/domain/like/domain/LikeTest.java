package boogi.apiserver.domain.like.domain;

import boogi.apiserver.builder.TestLike;
import boogi.apiserver.builder.TestMember;
import boogi.apiserver.builder.TestPost;
import boogi.apiserver.builder.TestUser;
import boogi.apiserver.domain.like.exception.UnmatchedLikeUserException;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LikeTest {

    @Nested
    @DisplayName("좋아요 한 유저와 같은지 여부 판단")
    class ValidateLikedUser {
        private static final long USER_ID = 1l;
        private static final long ANOTHER_USER_ID = 2l;

        @Test
        @DisplayName("좋아요 한 유저와 같다면 성공한다.")
        void sameUserSuccess() {
            User user = TestUser.builder().id(USER_ID).build();
            Member member = TestMember.builder().user(user).build();

            Like like = TestLike.builder().member(member).build();

            like.validateLikedUser(USER_ID);
        }

        @Test
        @DisplayName("좋아요 한 유저와 다르다면 UnmatchedLikeUserException 발생")
        void notSameUserFail() {
            User user = TestUser.builder().id(USER_ID).build();
            Member member = TestMember.builder().user(user).build();

            Like like = TestLike.builder().member(member).build();

            assertThatThrownBy(() -> like.validateLikedUser(ANOTHER_USER_ID))
                    .isInstanceOf(UnmatchedLikeUserException.class);
        }
    }

    @Test
    @DisplayName("게시글의 좋아요 개수가 1 감소한다.")
    void removeLikeCount() {
        Post post = TestPost.builder().likeCount(1).build();
        Like like = TestLike.builder().post(post).build();

        like.removeLikeCount();

        assertThat(post.getLikeCount()).isZero();
    }
}