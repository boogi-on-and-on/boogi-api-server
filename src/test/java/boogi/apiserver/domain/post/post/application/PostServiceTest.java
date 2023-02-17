package boogi.apiserver.domain.post.post.application;

import boogi.apiserver.domain.comment.dao.CommentRepository;
import boogi.apiserver.domain.comment.domain.Comment;
import boogi.apiserver.domain.community.community.application.CommunityQueryService;
import boogi.apiserver.domain.community.community.application.CommunityValidationService;
import boogi.apiserver.domain.community.community.dao.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.hashtag.post.application.PostHashtagService;
import boogi.apiserver.domain.hashtag.post.domain.PostHashtag;
import boogi.apiserver.domain.like.application.LikeService;
import boogi.apiserver.domain.like.dao.LikeRepository;
import boogi.apiserver.domain.member.application.MemberQueryService;
import boogi.apiserver.domain.member.application.MemberValidationService;
import boogi.apiserver.domain.member.dao.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.exception.HasNotUpdateAuthorityException;
import boogi.apiserver.domain.member.exception.NotAuthorizedMemberException;
import boogi.apiserver.domain.post.post.dao.PostRepository;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.post.post.dto.request.CreatePost;
import boogi.apiserver.domain.post.post.dto.request.UpdatePost;
import boogi.apiserver.domain.post.postmedia.application.PostMediaQueryService;
import boogi.apiserver.domain.post.postmedia.dao.PostMediaRepository;
import boogi.apiserver.domain.post.postmedia.domain.PostMedia;
import boogi.apiserver.domain.post.postmedia.vo.PostMedias;
import boogi.apiserver.domain.user.dao.UserRepository;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.global.webclient.push.SendPushNotification;
import boogi.apiserver.utils.TestEmptyEntityGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

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
    private LikeService likeService;

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
            final Community community = TestEmptyEntityGenerator.Community();
            ReflectionTestUtils.setField(community, "id", 1L);

            given(communityRepository.findByCommunityId(anyLong()))
                    .willReturn(community);

            final Member member = TestEmptyEntityGenerator.Member();
            ReflectionTestUtils.setField(member, "id", 1L);
            ReflectionTestUtils.setField(member, "community", community);

            given(memberQueryService.getJoinedMember(anyLong(), anyLong()))
                    .willReturn(member);

            final Post post = TestEmptyEntityGenerator.Post();
            ReflectionTestUtils.setField(post, "id", 1L);
            ReflectionTestUtils.setField(post, "community", community);

            given(postRepository.save(any(Post.class)))
                    .willReturn(post);

            given(postMediaQueryService.getUnmappedPostMediasByUUID(anyList()))
                    .willReturn(PostMedias.EMPTY);

            CreatePost createPost = new CreatePost(community.getId(), "내용", List.of(), List.of(), List.of());
            Post newPost = postService.createPost(createPost, 1L);

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
            final User user = TestEmptyEntityGenerator.User();
            ReflectionTestUtils.setField(user, "id", 1L);

            final Member member = TestEmptyEntityGenerator.Member();
            ReflectionTestUtils.setField(member, "id", 1L);
            ReflectionTestUtils.setField(member, "user", user);

            final Community community = TestEmptyEntityGenerator.Community();
            ReflectionTestUtils.setField(community, "id", 1L);

            final Post post = TestEmptyEntityGenerator.Post();
            ReflectionTestUtils.setField(post, "id", 1L);
            ReflectionTestUtils.setField(post, "community", community);
            ReflectionTestUtils.setField(post, "member", member);

            given(postQueryService.getPost(anyLong()))
                    .willReturn(post);

            given(communityRepository.findByCommunityId(anyLong()))
                    .willReturn(community);

            UpdatePost updatePost = new UpdatePost("글", List.of(), List.of());

            assertThatThrownBy(() -> postService.updatePost(updatePost, post.getId(), 2L))
                    .isInstanceOf(HasNotUpdateAuthorityException.class);
        }

        @Test
        @DisplayName("성공적으로 수정된다.")
        void UpdatePostSuccess() {
            final User user = TestEmptyEntityGenerator.User();
            ReflectionTestUtils.setField(user, "id", 1L);

            final Member member = TestEmptyEntityGenerator.Member();
            ReflectionTestUtils.setField(member, "id", 1L);
            ReflectionTestUtils.setField(member, "user", user);

            final Community community = TestEmptyEntityGenerator.Community();
            ReflectionTestUtils.setField(community, "id", 1L);

            given(communityRepository.findByCommunityId(anyLong()))
                    .willReturn(community);

            final Post post = TestEmptyEntityGenerator.Post();
            ReflectionTestUtils.setField(post, "id", 1L);
            ReflectionTestUtils.setField(post, "community", community);
            ReflectionTestUtils.setField(post, "member", member);
            ReflectionTestUtils.setField(post, "content", "글");

            given(postQueryService.getPost(anyLong()))
                    .willReturn(post);

            final PostHashtag postHashtag = TestEmptyEntityGenerator.PostHashtag();
            ReflectionTestUtils.setField(postHashtag, "id", 1L);
            ReflectionTestUtils.setField(postHashtag, "post", post);
            ReflectionTestUtils.setField(postHashtag, "tag", "해시태그");

            given(postHashtagService.addTags(anyLong(), anyList()))
                    .willReturn(List.of(postHashtag));

            final PostMedia postMedia = TestEmptyEntityGenerator.PostMedia();
            ReflectionTestUtils.setField(postMedia, "id", 1L);
            ReflectionTestUtils.setField(postMedia, "uuid", "uuid");

            given(postMediaRepository.findByPostId(anyLong()))
                    .willReturn(List.of());

            given(postMediaQueryService.getUnmappedPostMediasByUUID(anyList()))
                    .willReturn(new PostMedias(List.of(postMedia)));

            UpdatePost updatePost = new UpdatePost("수정글", List.of(postHashtag.getTag()), List.of(postMedia.getUuid()));

            Post updatedPost = postService.updatePost(updatePost, post.getId(), 1L);

            assertThat(updatedPost.getId()).isEqualTo(post.getId());
            assertThat(updatedPost.getHashtags().size()).isEqualTo(1);
            assertThat(updatedPost.getHashtags().get(0).getId()).isEqualTo(postHashtag.getId());
            assertThat(updatedPost.getHashtags().get(0).getTag()).isEqualTo(postHashtag.getTag());
            assertThat(updatedPost.getPostMedias().size()).isEqualTo(1);
            assertThat(updatedPost.getPostMedias().get(0).getId()).isEqualTo(postMedia.getId());
            assertThat(updatedPost.getPostMedias().get(0).getUuid()).isEqualTo(postMedia.getUuid());
            assertThat(updatedPost.getContent()).isEqualTo(updatePost.getContent());
        }
    }

    @Nested
    @DisplayName("글 삭제시")
    class DeletePostTest {

        @Test
        @DisplayName("성공적으로 삭제된다.")
        void deletePostSuccess() {
            final User user = TestEmptyEntityGenerator.User();
            ReflectionTestUtils.setField(user, "id", 1L);

            final Member member = TestEmptyEntityGenerator.Member();
            ReflectionTestUtils.setField(member, "id", 1L);
            ReflectionTestUtils.setField(member, "user", user);

            final Community community = TestEmptyEntityGenerator.Community();
            ReflectionTestUtils.setField(community, "id", 1L);

            final Post post = TestEmptyEntityGenerator.Post();
            ReflectionTestUtils.setField(post, "id", 1L);
            ReflectionTestUtils.setField(post, "community", community);
            ReflectionTestUtils.setField(post, "member", member);
            ReflectionTestUtils.setField(post, "commentCount", 1);

            given(postRepository.getPostWithCommunityAndMemberByPostId(anyLong()))
                    .willReturn(Optional.of(post));

            final Comment comment = TestEmptyEntityGenerator.Comment();
            ReflectionTestUtils.setField(comment, "id", 1L);
            ReflectionTestUtils.setField(comment, "post", post);

            given(commentRepository.findByPostId(anyLong()))
                    .willReturn(List.of(comment));

            final PostMedia postMedia = TestEmptyEntityGenerator.PostMedia();
            ReflectionTestUtils.setField(postMedia, "id", 1L);

            List<PostMedia> postMedias = List.of(postMedia);
            given(postMediaRepository.findByPostId(anyLong()))
                    .willReturn(postMedias);

            postService.deletePost(post.getId(), 1L);

            verify(postHashtagService, times(1)).removeTagsByPostId(post.getId());
            verify(postMediaRepository, times(1)).deleteAllInBatch(postMedias);
            verify(postRepository, times(1)).delete(post);

            assertThat(comment.getDeletedAt()).isNotNull();
        }

        @Test
        @DisplayName("글 작성자가 본인이 아니거나, 해당 커뮤니티 (부)매니저가 아닐 경우 NotAuthorizedMemberException 발생한다.")
        void notAuthorizedMemberFail() {
            final User user = TestEmptyEntityGenerator.User();
            ReflectionTestUtils.setField(user, "id", 1L);

            final Member member = TestEmptyEntityGenerator.Member();
            ReflectionTestUtils.setField(member, "id", 1L);
            ReflectionTestUtils.setField(member, "user", user);

            final Community community = TestEmptyEntityGenerator.Community();
            ReflectionTestUtils.setField(community, "id", 1L);

            final Post post = TestEmptyEntityGenerator.Post();
            ReflectionTestUtils.setField(post, "id", 1L);
            ReflectionTestUtils.setField(post, "community", community);
            ReflectionTestUtils.setField(post, "member", member);

            given(postRepository.getPostWithCommunityAndMemberByPostId(anyLong()))
                    .willReturn(Optional.of(post));

            given(memberValidationService.hasSubManagerAuthority(anyLong(), anyLong()))
                    .willReturn(false);

            assertThatThrownBy(() -> postService.deletePost(post.getId(), 2L))
                    .isInstanceOf(NotAuthorizedMemberException.class);
        }
    }
}