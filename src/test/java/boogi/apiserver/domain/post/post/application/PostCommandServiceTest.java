package boogi.apiserver.domain.post.post.application;

import boogi.apiserver.builder.*;
import boogi.apiserver.domain.comment.dao.CommentRepository;
import boogi.apiserver.domain.comment.domain.Comment;
import boogi.apiserver.domain.community.community.dao.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.hashtag.post.domain.PostHashtag;
import boogi.apiserver.domain.like.application.LikeCommandService;
import boogi.apiserver.domain.member.application.MemberQueryService;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.member.exception.CanNotDeletePostException;
import boogi.apiserver.domain.member.exception.CanNotUpdatePostException;
import boogi.apiserver.domain.post.post.dao.PostRepository;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.post.post.dto.request.CreatePostRequest;
import boogi.apiserver.domain.post.post.dto.request.UpdatePostRequest;
import boogi.apiserver.domain.post.postmedia.application.PostMediaQueryService;
import boogi.apiserver.domain.post.postmedia.dao.PostMediaRepository;
import boogi.apiserver.domain.post.postmedia.domain.PostMedia;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class PostCommandServiceTest {
    @InjectMocks
    PostCommandService postCommandService;

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private CommunityRepository communityRepository;

    @Mock
    private PostMediaRepository postMediaRepository;

    @Mock
    private LikeCommandService likeCommandService;

    @Mock
    private MemberQueryService memberQueryService;

    @Mock
    private PostMediaQueryService postMediaQueryService;

    @Nested
    @DisplayName("글 생성시")
    class CreatePostTest {
        private static final String NEW_POST_CONTENT = "게시글의 내용입니다.";

        @Captor
        ArgumentCaptor<Post> postCaptor;

        @Test
        @DisplayName("성공적으로 글이 생성된다.")
        void createPostSuccess() {

            final Community community = TestCommunity.builder().id(1L).build();

            final Member member = TestMember.builder()
                    .id(2L)
                    .community(community)
                    .build();

            given(communityRepository.findCommunityById(anyLong()))
                    .willReturn(community);
            given(memberQueryService.getMember(anyLong(), anyLong()))
                    .willReturn(member);
            given(postMediaQueryService.getUnmappedPostMediasByUUID(anyList()))
                    .willReturn(List.of());

            CreatePostRequest createPostRequest =
                    new CreatePostRequest(1L, NEW_POST_CONTENT, List.of(), List.of(), List.of());

            postCommandService.createPost(createPostRequest, 1L);

            verify(postRepository, times(1)).save(postCaptor.capture());

            Post newPost = postCaptor.getValue();
            assertThat(newPost.getContent()).isEqualTo(NEW_POST_CONTENT);
            assertThat(newPost.getCommunityId()).isEqualTo(1L);
            assertThat(newPost.getPostMedias().isEmpty()).isTrue();
            assertThat(newPost.getHashtags().isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("글 수정시")
    class UpdatePostTest {
        private static final String BEFORE_POST_CONTENT = "이전 게시글의 내용";
        private static final String UPDATE_POST_CONTENT = "바꿀 게시글의 내용";
        private static final String POSTHASHTAG_TAG = "해시태그";
        private static final String POSTMEDIA_UUID = "uuid";


        @Test
        @DisplayName("글 작성자 본인이 아닌 유저가 요청하는 경우 CanNotUpdatePostException 예외가 발생한다.")
        void notAuthorizedFail() {
            final User user = TestUser.builder().id(1L).build();

            final Member member = TestMember.builder()
                    .id(2L)
                    .user(user)
                    .build();

            final Post post = TestPost.builder()
                    .id(3L)
                    .member(member)
                    .build();

            given(postRepository.findPostById(anyLong()))
                    .willReturn(post);

            UpdatePostRequest updatePostRequest = new UpdatePostRequest(UPDATE_POST_CONTENT, List.of(), List.of());

            assertThatThrownBy(() -> postCommandService.updatePost(updatePostRequest, 3L, 2L))
                    .isInstanceOf(CanNotUpdatePostException.class);
        }

        @Test
        @DisplayName("성공적으로 수정된다.")
        void updatePostSuccess() {
            final User user = TestUser.builder().id(1L).build();

            final Member member = TestMember.builder()
                    .id(2L)
                    .user(user)
                    .build();

            final Post post = TestPost.builder()
                    .id(3L)
                    .member(member)
                    .content(BEFORE_POST_CONTENT)
                    .build();

            final PostMedia postMedia = TestPostMedia.builder()
                    .id(4L)
                    .uuid(POSTMEDIA_UUID)
                    .build();

            given(postRepository.findPostById(anyLong()))
                    .willReturn(post);
            given(postMediaRepository.findByUuidIn(anyList()))
                    .willReturn(List.of(postMedia));

            UpdatePostRequest request =
                    new UpdatePostRequest(UPDATE_POST_CONTENT, List.of(POSTHASHTAG_TAG), List.of(POSTMEDIA_UUID));

            postCommandService.updatePost(request, 3L, 1L);

            assertThat(post.getContent()).isEqualTo(UPDATE_POST_CONTENT);

            final List<PostMedia> medias = post.getPostMedias();
            final List<PostHashtag> hashtags = post.getHashtags();
            assertThat(hashtags.size()).isEqualTo(1);
            assertThat(hashtags.get(0).getTag()).isEqualTo(POSTHASHTAG_TAG);
            assertThat(medias.size()).isEqualTo(1);
            assertThat(medias.get(0).getId()).isEqualTo(4L);
            assertThat(medias.get(0).getUuid()).isEqualTo(POSTMEDIA_UUID);
        }
    }

    @Nested
    @DisplayName("글 삭제시")
    class DeletePostTest {
        @Test
        @DisplayName("성공적으로 삭제된다.")
        void deletePostSuccess() {
            final User user = TestUser.builder().id(1L).build();

            final Member member = TestMember.builder()
                    .id(1L)
                    .user(user)
                    .build();

            final Community community = TestCommunity.builder().id(1L).build();

            final Post post = TestPost.builder()
                    .id(1L)
                    .community(community)
                    .member(member)
                    .commentCount(1)
                    .build();

            final Comment comment = TestComment.builder()
                    .id(1L)
                    .post(post)
                    .build();

            given(postRepository.findPostById(anyLong()))
                    .willReturn(post);
            given(commentRepository.findByPostId(anyLong()))
                    .willReturn(List.of(comment));

            postCommandService.deletePost(post.getId(), 1L);

            verify(likeCommandService, times(1)).removePostLikes(anyLong());
            verify(postRepository, times(1)).delete(post);

            assertThat(comment.getDeletedAt()).isNotNull();
        }

        @Test
        @DisplayName("글 작성자가 본인이 아니거나, 해당 커뮤니티 (부)매니저가 아닐 경우 CanNotDeletePostException 발생한다.")
        void notAuthorizedMemberFail() {
            final User user = TestUser.builder().id(1L).build();

            final Community community = TestCommunity.builder().id(2L).build();

            final Member authorMember = TestMember.builder()
                    .id(3L)
                    .community(community)
                    .user(user)
                    .build();
            final Member notAuthorNormalMember = TestMember.builder()
                    .id(4L)
                    .community(community)
                    .memberType(MemberType.NORMAL)
                    .build();

            final Post post = TestPost.builder()
                    .id(5L)
                    .community(community)
                    .member(authorMember)
                    .build();

            given(postRepository.findPostById(anyLong()))
                    .willReturn(post);
            given(memberQueryService.getMember(anyLong(), anyLong()))
                    .willReturn(notAuthorNormalMember);

            assertThatThrownBy(() -> postCommandService.deletePost(post.getId(), 2L))
                    .isInstanceOf(CanNotDeletePostException.class);
        }
    }
}