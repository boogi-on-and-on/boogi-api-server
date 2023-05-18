package boogi.apiserver.domain.post.post.application;

import boogi.apiserver.domain.comment.domain.Comment;
import boogi.apiserver.domain.comment.repository.CommentRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.community.community.repository.CommunityRepository;
import boogi.apiserver.domain.hashtag.post.domain.PostHashtag;
import boogi.apiserver.domain.like.application.LikeCommand;
import boogi.apiserver.domain.member.application.MemberQuery;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.exception.CanNotDeletePostException;
import boogi.apiserver.domain.member.exception.CanNotUpdatePostException;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.post.post.dto.request.CreatePostRequest;
import boogi.apiserver.domain.post.post.dto.request.UpdatePostRequest;
import boogi.apiserver.domain.post.post.repository.PostRepository;
import boogi.apiserver.domain.post.postmedia.application.PostMediaQuery;
import boogi.apiserver.domain.post.postmedia.domain.PostMedia;
import boogi.apiserver.domain.post.postmedia.repository.PostMediaRepository;
import boogi.apiserver.domain.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static boogi.apiserver.utils.fixture.CommentFixture.COMMENT1;
import static boogi.apiserver.utils.fixture.CommunityFixture.POCS;
import static boogi.apiserver.utils.fixture.MemberFixture.DEOKHWAN_POCS;
import static boogi.apiserver.utils.fixture.MemberFixture.SUNDO_POCS;
import static boogi.apiserver.utils.fixture.PostFixture.POST1;
import static boogi.apiserver.utils.fixture.PostMediaFixture.POSTMEDIA1;
import static boogi.apiserver.utils.fixture.UserFixture.SUNDO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class PostCommandTest {
    @InjectMocks
    PostCommand postCommand;

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private CommunityRepository communityRepository;

    @Mock
    private PostMediaRepository postMediaRepository;

    @Mock
    private LikeCommand likeCommand;

    @Mock
    private MemberQuery memberQuery;

    @Mock
    private PostMediaQuery postMediaQuery;

    private final User user = SUNDO.toUser(1L);
    private final Community community = POCS.toCommunity(2L, List.of());
    private final Member member = SUNDO_POCS.toMember(3L, user, community);

    @Nested
    @DisplayName("글 생성시")
    class CreatePostTest {
        private static final String NEW_POST_CONTENT = "게시글의 내용입니다.";

        @Captor
        ArgumentCaptor<Post> postCaptor;

        @Test
        @DisplayName("성공적으로 글이 생성된다.")
        void createPostSuccess() {
            given(communityRepository.findCommunityById(anyLong())).willReturn(community);
            given(memberQuery.getMember(anyLong(), anyLong())).willReturn(member);
            given(postMediaQuery.getUnmappedPostMediasByUUID(anyList())).willReturn(List.of());

            CreatePostRequest createPostRequest =
                    new CreatePostRequest(1L, NEW_POST_CONTENT, List.of(), List.of(), List.of());

            postCommand.createPost(createPostRequest, 1L);

            verify(postRepository, times(1)).save(postCaptor.capture());

            Post newPost = postCaptor.getValue();
            assertThat(newPost.getContent()).isEqualTo(NEW_POST_CONTENT);
            assertThat(newPost.getCommunityId()).isEqualTo(2L);
            assertThat(newPost.getPostMedias().isEmpty()).isTrue();
            assertThat(newPost.getHashtags().isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("글 수정시")
    class UpdatePostTest {
        private static final String UPDATE_POST_CONTENT = "바꿀 게시글의 내용";
        private static final String UPDATE_POSTHASHTAG = "해시태그";

        @Test
        @DisplayName("글 작성자 본인이 아닌 유저가 요청하는 경우 CanNotUpdatePostException 예외가 발생한다.")
        void notAuthorizedFail() {
            Post post = POST1.toPost(4L, member, community, List.of(), List.of());

            given(postRepository.findPostById(anyLong())).willReturn(post);

            UpdatePostRequest updatePostRequest = new UpdatePostRequest(UPDATE_POST_CONTENT, List.of(), List.of());

            assertThatThrownBy(() -> postCommand.updatePost(updatePostRequest, 3L, 2L))
                    .isInstanceOf(CanNotUpdatePostException.class);
        }

        @Test
        @DisplayName("성공적으로 수정된다.")
        void updatePostSuccess() {
            Post post = POST1.toPost(4L, member, community, List.of(), List.of());
            PostMedia postMedia = POSTMEDIA1.toPostMedia(5L);

            given(postRepository.findPostById(anyLong())).willReturn(post);
            given(postMediaRepository.findByUuidIn(anyList())).willReturn(List.of(postMedia));

            UpdatePostRequest request =
                    new UpdatePostRequest(UPDATE_POST_CONTENT, List.of(UPDATE_POSTHASHTAG), List.of(POSTMEDIA1.uuid));

            postCommand.updatePost(request, 3L, 1L);

            assertThat(post.getContent()).isEqualTo(UPDATE_POST_CONTENT);

            final List<PostMedia> medias = post.getPostMedias();
            final List<PostHashtag> hashtags = post.getHashtags();
            assertThat(hashtags).hasSize(1);
            assertThat(hashtags.get(0).getTag()).isEqualTo(UPDATE_POSTHASHTAG);
            assertThat(medias).hasSize(1);
            assertThat(medias.get(0).getId()).isEqualTo(5L);
            assertThat(medias.get(0).getPost().getId()).isEqualTo(4L);
            assertThat(medias.get(0).getUuid()).isEqualTo(POSTMEDIA1.uuid);
        }
    }

    @Nested
    @DisplayName("글 삭제시")
    class DeletePostTest {
        @Test
        @DisplayName("성공적으로 삭제된다.")
        void deletePostSuccess() {
            Post post = POST1.toPost(4L, member, community, List.of(), List.of());
            Comment comment = COMMENT1.toComment(5L, post, member, null);

            given(postRepository.findPostById(anyLong())).willReturn(post);
            given(commentRepository.findByPostId(anyLong())).willReturn(List.of(comment));

            postCommand.deletePost(post.getId(), 1L);

            verify(likeCommand, times(1)).removePostLikes(anyLong());
            verify(postRepository, times(1)).delete(post);

            assertThat(comment.getDeletedAt()).isNotNull();
        }

        @Test
        @DisplayName("글 작성자가 본인이 아니거나, 해당 커뮤니티 (부)매니저가 아닐 경우 CanNotDeletePostException 발생한다.")
        void notAuthorizedMemberFail() {
            Member notAuthorizedMember = DEOKHWAN_POCS.toMember(4L, user, community);

            Post post = POST1.toPost(6L, member, community, List.of(), List.of());

            given(postRepository.findPostById(anyLong())).willReturn(post);
            given(memberQuery.getMember(anyLong(), anyLong())).willReturn(notAuthorizedMember);

            assertThatThrownBy(() -> postCommand.deletePost(post.getId(), 2L))
                    .isInstanceOf(CanNotDeletePostException.class);
        }
    }
}