package boogi.apiserver.domain.post.post.application;

import boogi.apiserver.builder.*;
import boogi.apiserver.domain.comment.dao.CommentRepository;
import boogi.apiserver.domain.comment.domain.Comment;
import boogi.apiserver.domain.community.community.application.CommunityQueryService;
import boogi.apiserver.domain.community.community.application.CommunityValidationService;
import boogi.apiserver.domain.community.community.dao.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.hashtag.post.application.PostHashtagService;
import boogi.apiserver.domain.hashtag.post.domain.PostHashtag;
import boogi.apiserver.domain.like.application.LikeCommandService;
import boogi.apiserver.domain.like.dao.LikeRepository;
import boogi.apiserver.domain.member.application.MemberQueryService;
import boogi.apiserver.domain.member.application.MemberValidationService;
import boogi.apiserver.domain.member.dao.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.exception.HasNotUpdateAuthorityException;
import boogi.apiserver.domain.member.exception.NotAuthorizedMemberException;
import boogi.apiserver.domain.post.post.dao.PostRepository;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.post.post.dto.request.CreatePostRequest;
import boogi.apiserver.domain.post.post.dto.request.UpdatePostRequest;
import boogi.apiserver.domain.post.postmedia.application.PostMediaQueryService;
import boogi.apiserver.domain.post.postmedia.dao.PostMediaRepository;
import boogi.apiserver.domain.post.postmedia.domain.PostMedia;
import boogi.apiserver.domain.post.postmedia.vo.PostMedias;
import boogi.apiserver.domain.user.dao.UserRepository;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.global.webclient.push.SendPushNotification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
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
    private MemberRepository memberRepository;

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private CommunityRepository communityRepository;

    @Mock
    private PostMediaRepository postMediaRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommunityValidationService communityValidationService;

    @Mock
    private MemberValidationService memberValidationService;

    @Mock
    private PostHashtagService postHashtagService;

    @Mock
    private LikeCommandService likeCommandService;

    @Mock
    private MemberQueryService memberQueryService;

    @Mock
    private PostQueryService postQueryService;

    @Mock
    private PostMediaQueryService postMediaQueryService;

    @Mock
    private CommunityQueryService communityQueryService;

    @Mock
    private SendPushNotification sendPushNotification;


    @Nested
    @DisplayName("글 생성시")
    class CreatePostTest {

        @Test
        @DisplayName("성공적으로 글이 생성된다.")
        void createPostSuccess() {
            final Community community = TestCommunity.builder().id(1L).build();
            given(communityRepository.findByCommunityId(anyLong()))
                    .willReturn(community);

            final Member member = TestMember.builder()
                    .id(1L)
                    .community(community)
                    .build();
            given(memberQueryService.getJoinedMember(anyLong(), anyLong()))
                    .willReturn(member);

            final Post post = TestPost.builder()
                    .id(1L)
                    .community(community)
                    .build();
            given(postRepository.save(any(Post.class)))
                    .willReturn(post);

            given(postMediaQueryService.getUnmappedPostMediasByUUID(anyList()))
                    .willReturn(PostMedias.EMPTY);

            CreatePostRequest createPostRequest = new CreatePostRequest(community.getId(), "게시글의 내용입니다.", List.of(), List.of(), List.of());
            Post newPost = postCommandService.createPost(createPostRequest, 1L);

            verify(postHashtagService, times(1)).addTags(anyLong(), anyList());

            assertThat(newPost).isEqualTo(post);
        }
    }

    @Nested
    @DisplayName("글 수정시")
    class UpdatePostTest {

        @Test
        @DisplayName("글 작성자 본인이 아닌 유저가 요청하는 경우 HasNotUpdateAuthorityException 발생한다.")
        void notAuthorizedFail() {
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
                    .build();
            given(postRepository.findByPostId(anyLong()))
                    .willReturn(post);

            given(communityRepository.findByCommunityId(anyLong()))
                    .willReturn(community);

            UpdatePostRequest updatePostRequest = new UpdatePostRequest("게시글의 내용입니다.", List.of(), List.of());

            assertThatThrownBy(() -> postCommandService.updatePost(updatePostRequest, post.getId(), 2L))
                    .isInstanceOf(HasNotUpdateAuthorityException.class);
        }

        @Test
        @DisplayName("성공적으로 수정된다.")
        void UpdatePostSuccess() {
            final User user = TestUser.builder().id(1L).build();

            final Member member = TestMember.builder()
                    .id(1L)
                    .user(user)
                    .build();

            final Community community = TestCommunity.builder().id(1L).build();
            given(communityRepository.findByCommunityId(anyLong()))
                    .willReturn(community);

            final Post post = TestPost.builder()
                    .id(1L)
                    .community(community)
                    .member(member)
                    .content("이전 게시글의 내용")
                    .build();
            given(postRepository.findByPostId(anyLong()))
                    .willReturn(post);

            final PostHashtag postHashtag = TestPostHashtag.builder()
                    .id(1L)
                    .post(post)
                    .tag("해시태그")
                    .build();
            given(postHashtagService.addTags(anyLong(), anyList()))
                    .willReturn(List.of(postHashtag));

            final PostMedia postMedia = TestPostMedia.builder()
                    .id(1L)
                    .uuid("uuid")
                    .build();
            given(postMediaRepository.findByPostId(anyLong()))
                    .willReturn(List.of());

            given(postMediaQueryService.getUnmappedPostMediasByUUID(anyList()))
                    .willReturn(new PostMedias(List.of(postMedia)));

            UpdatePostRequest updatePostRequest = new UpdatePostRequest("게시글의 내용입니다.", List.of(postHashtag.getTag()), List.of(postMedia.getUuid()));

            Post updatedPost = postCommandService.updatePost(updatePostRequest, post.getId(), 1L);

            final List<PostMedia> medias = updatedPost.getPostMedias().getValues();
            final List<PostHashtag> hashtags = updatedPost.getHashtags().getValues();
            assertThat(updatedPost.getId()).isEqualTo(post.getId());
            assertThat(hashtags.size()).isEqualTo(1);
            assertThat(hashtags.get(0).getId()).isEqualTo(postHashtag.getId());
            assertThat(hashtags.get(0).getTag()).isEqualTo(postHashtag.getTag());
            assertThat(medias.size()).isEqualTo(1);
            assertThat(medias.get(0).getId()).isEqualTo(postMedia.getId());
            assertThat(medias.get(0).getUuid()).isEqualTo(postMedia.getUuid());
            assertThat(updatedPost.getContent()).isEqualTo(updatePostRequest.getContent());
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
            given(postRepository.getPostWithCommunityAndMemberByPostId(anyLong()))
                    .willReturn(Optional.of(post));

            final Comment comment = TestComment.builder()
                    .id(1L)
                    .post(post)
                    .build();
            given(commentRepository.findByPostId(anyLong()))
                    .willReturn(List.of(comment));

            final PostMedia postMedia = TestPostMedia.builder().id(1L).build();
            List<PostMedia> postMedias = List.of(postMedia);
            given(postMediaRepository.findByPostId(anyLong()))
                    .willReturn(postMedias);

            postCommandService.deletePost(post.getId(), 1L);

            verify(postHashtagService, times(1)).removeTagsByPostId(post.getId());
            verify(postMediaRepository, times(1)).deleteAllInBatch(postMedias);
            verify(postRepository, times(1)).delete(post);

            assertThat(comment.getDeletedAt()).isNotNull();
        }

        @Test
        @DisplayName("글 작성자가 본인이 아니거나, 해당 커뮤니티 (부)매니저가 아닐 경우 NotAuthorizedMemberException 발생한다.")
        void notAuthorizedMemberFail() {
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
                    .build();
            given(postRepository.getPostWithCommunityAndMemberByPostId(anyLong()))
                    .willReturn(Optional.of(post));

            given(memberValidationService.hasSubManagerAuthority(anyLong(), anyLong()))
                    .willReturn(false);

            assertThatThrownBy(() -> postCommandService.deletePost(post.getId(), 2L))
                    .isInstanceOf(NotAuthorizedMemberException.class);
        }
    }
}