package boogi.apiserver.domain.post.post.application;

import boogi.apiserver.domain.comment.dao.CommentRepository;
import boogi.apiserver.domain.comment.domain.Comment;
import boogi.apiserver.domain.community.community.application.CommunityQueryService;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.hashtag.post.application.PostHashtagCoreService;
import boogi.apiserver.domain.hashtag.post.domain.PostHashtag;
import boogi.apiserver.domain.like.application.LikeCoreService;
import boogi.apiserver.domain.member.application.MemberQueryService;
import boogi.apiserver.domain.member.application.MemberValidationService;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.exception.HasNotDeleteAuthorityException;
import boogi.apiserver.domain.member.exception.NotAuthorizedMemberException;
import boogi.apiserver.domain.post.post.dao.PostRepository;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.post.post.dto.request.CreatePost;
import boogi.apiserver.domain.post.post.dto.request.UpdatePost;
import boogi.apiserver.domain.post.postmedia.application.PostMediaQueryService;
import boogi.apiserver.domain.post.postmedia.dao.PostMediaRepository;
import boogi.apiserver.domain.post.postmedia.domain.PostMedia;
import boogi.apiserver.domain.post.postmedia.vo.PostMedias;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.global.webclient.push.SendPushNotification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class PostServiceTest {
    @InjectMocks
    PostService postService;

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostMediaRepository postMediaRepository;

    @Mock
    private MemberValidationService memberValidationService;

    @Mock
    private PostHashtagCoreService postHashtagCoreService;

    @Mock
    private LikeCoreService likeCoreService;

    @Mock
    private PostMediaQueryService postMediaQueryService;

    @Mock
    private CommunityQueryService communityQueryService;

    @Mock
    private PostQueryService postQueryService;

    @Mock
    private MemberQueryService memberQueryService;

    @Mock
    private SendPushNotification sendPushNotification;


    @Nested
    @DisplayName("글 생성시")
    class CreatePostTest {

        @Test
        @DisplayName("성공적으로 글이 생성된다.")
        void createPostSuccess() {
            Community community = Community.builder()
                    .id(1L)
                    .build();
            given(communityQueryService.getCommunity(anyLong()))
                    .willReturn(community);

            Member member = Member.builder()
                    .id(2L)
                    .community(community)
                    .build();
            given(memberQueryService.getJoinedMember(anyLong(), anyLong()))
                    .willReturn(member);

            Post post = Post.builder()
                    .id(3L)
                    .community(community)
                    .build();
            given(postRepository.save(any(Post.class)))
                    .willReturn(post);

            given(postMediaQueryService.getUnmappedPostMediasByUUID(anyList()))
                    .willReturn(PostMedias.EMPTY);

            CreatePost createPost = new CreatePost(community.getId(), "내용", List.of(), List.of(), List.of());
            Post newPost = postService.createPost(createPost, 4L);

            assertThat(newPost).isEqualTo(post);
        }
    }

    @Nested
    @DisplayName("글 수정시")
    class UpdatePostTest {

        @Test
        @DisplayName("글 작성자 본인이 아닌 유저가 요청하는 경우 NotAuthorizedException 발생한다.")
        void notAuthorizedFail() {
            User user = User.builder()
                    .id(1L)
                    .build();

            Member member = Member.builder()
                    .id(2L)
                    .user(user)
                    .build();

            Community community = Community.builder()
                    .id(3L)
                    .build();

            Post post = Post.builder()
                    .id(4L)
                    .member(member)
                    .community(community)
                    .build();
            given(postQueryService.getPost(anyLong()))
                    .willReturn(post);

            given(communityQueryService.getCommunity(anyLong()))
                    .willReturn(community);

            UpdatePost updatePost = new UpdatePost("글", List.of(), List.of());

            assertThatThrownBy(() -> postService.updatePost(updatePost, post.getId(), 2L))
                    .isInstanceOf(NotAuthorizedMemberException.class);
        }

        @Test
        @DisplayName("성공적으로 수정된다.")
        void UpdatePostSuccess() {
            User user = User.builder()
                    .id(1L)
                    .build();

            Member member = Member.builder()
                    .id(2L)
                    .user(user)
                    .build();

            Community community = Community.builder()
                    .id(3L)
                    .build();
            given(communityQueryService.getCommunity(anyLong()))
                    .willReturn(community);

            Post post = Post.builder()
                    .id(4L)
                    .member(member)
                    .community(community)
                    .content("글")
                    .postMedias(new ArrayList<>())
                    .hashtags(new ArrayList<>())
                    .build();
            given(postQueryService.getPost(anyLong()))
                    .willReturn(post);

            PostHashtag postHashtag = PostHashtag.builder()
                    .id(5L)
                    .tag("해시태그")
                    .post(post)
                    .build();
            given(postHashtagCoreService.addTags(anyLong(), anyList()))
                    .willReturn(List.of(postHashtag));
            PostMedia postMedia = PostMedia.builder()
                    .id(6L)
                    .uuid("uuid")
                    .build();
            given(postMediaRepository.findByPostId(anyLong()))
                    .willReturn(List.of());

            given(postMediaQueryService.getUnmappedPostMediasByUUID(anyList()))
                    .willReturn(new PostMedias(List.of(postMedia)));

            UpdatePost updatePost = new UpdatePost("수정글", List.of(postHashtag.getTag()), List.of(postMedia.getUuid()));

            Post updatedPost = postService.updatePost(updatePost, post.getId(), 1L);

            assertThat(updatedPost.getId()).isEqualTo(4L);
            assertThat(updatedPost.getContent()).isEqualTo("수정글");

            assertThat(updatedPost.getHashtags().size()).isEqualTo(1);
            assertThat(updatedPost.getHashtags().get(0).getId()).isEqualTo(5L);
            assertThat(updatedPost.getHashtags().get(0).getTag()).isEqualTo("해시태그");

            assertThat(updatedPost.getPostMedias().size()).isEqualTo(1);
            assertThat(updatedPost.getPostMedias().get(0).getId()).isEqualTo(6L);
            assertThat(updatedPost.getPostMedias().get(0).getUuid()).isEqualTo("uuid");
        }
    }

    @Nested
    @DisplayName("글 삭제시")
    class DeletePostTest {

        @Test
        @DisplayName("성공적으로 삭제된다.")
        void deletePostSuccess() {
            User user = User.builder()
                    .id(1L)
                    .build();

            Member member = Member.builder()
                    .id(2L)
                    .user(user)
                    .build();

            Community community = Community.builder()
                    .id(3L)
                    .build();

            Post post = Post.builder()
                    .id(4L)
                    .member(member)
                    .community(community)
                    .commentCount(1)
                    .build();
            given(postRepository.getPostWithCommunityAndMemberByPostId(anyLong()))
                    .willReturn(Optional.of(post));

            Comment comment = Comment.builder()
                    .id(5L)
                    .post(post)
                    .build();
            given(commentRepository.findByPostId(anyLong()))
                    .willReturn(List.of(comment));

            PostMedia postMedia = PostMedia.builder()
                    .id(6L)
                    .build();
            List<PostMedia> postMedias = List.of(postMedia);
            given(postMediaRepository.findByPostId(anyLong()))
                    .willReturn(postMedias);

            postService.deletePost(post.getId(), 1L);

            verify(postHashtagCoreService, times(1)).removeTagsByPostId(4L);
            verify(postMediaRepository, times(1)).deleteAllInBatch(postMedias);
            verify(postRepository, times(1)).delete(post);

            assertThat(comment.getDeletedAt()).isNotNull();
        }

        @Test
        @DisplayName("글 작성자가 본인이 아니거나, 해당 커뮤니티 (부)매니저가 아닐 경우 NotAuthorizedMemberException 발생한다.")
        void notAuthorizedMemberFail() {
            User user = User.builder()
                    .id(1L)
                    .build();

            Member member = Member.builder()
                    .id(2L)
                    .user(user)
                    .build();

            Community community = Community.builder()
                    .id(3L)
                    .build();

            Post post = Post.builder()
                    .id(4L)
                    .member(member)
                    .community(community)
                    .build();
            given(postRepository.getPostWithCommunityAndMemberByPostId(anyLong()))
                    .willReturn(Optional.of(post));

            given(memberValidationService.hasSubManagerAuthority(anyLong(), anyLong()))
                    .willReturn(false);

            assertThatThrownBy(() -> postService.deletePost(post.getId(), 2L))
                    .isInstanceOf(HasNotDeleteAuthorityException.class);
        }
    }
}