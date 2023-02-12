package boogi.apiserver.domain.post.post.application;

import boogi.apiserver.domain.comment.dao.CommentRepository;
import boogi.apiserver.domain.comment.domain.Comment;
import boogi.apiserver.domain.community.community.application.CommunityQueryService;
import boogi.apiserver.domain.community.community.application.CommunityValidationService;
import boogi.apiserver.domain.community.community.dao.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.community.community.domain.CommunityCategory;
import boogi.apiserver.domain.hashtag.post.application.PostHashtagCoreService;
import boogi.apiserver.domain.hashtag.post.domain.PostHashtag;
import boogi.apiserver.domain.like.application.LikeCoreService;
import boogi.apiserver.domain.like.dao.LikeRepository;
import boogi.apiserver.domain.like.domain.Like;
import boogi.apiserver.domain.member.application.MemberValidationService;
import boogi.apiserver.domain.member.dao.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.member.exception.NotAuthorizedMemberException;
import boogi.apiserver.domain.member.exception.NotJoinedMemberException;
import boogi.apiserver.domain.post.post.dao.PostRepository;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.post.post.dto.request.CreatePost;
import boogi.apiserver.domain.post.post.dto.request.UpdatePost;
import boogi.apiserver.domain.post.post.dto.response.PostDetail;
import boogi.apiserver.domain.post.post.dto.response.UserPostPage;
import boogi.apiserver.domain.post.post.dto.response.UserPostsDto;
import boogi.apiserver.domain.post.postmedia.application.PostMediaQueryService;
import boogi.apiserver.domain.post.postmedia.dao.PostMediaRepository;
import boogi.apiserver.domain.post.postmedia.domain.MediaType;
import boogi.apiserver.domain.post.postmedia.domain.PostMedia;
import boogi.apiserver.domain.user.dao.UserRepository;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.global.dto.PaginationDto;
import boogi.apiserver.global.webclient.push.SendPushNotification;
import boogi.apiserver.utils.TestEmptyEntityGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static boogi.apiserver.domain.post.postmedia.domain.MediaType.IMG;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class PostCoreServiceTest {
    @InjectMocks
    PostCoreService postCoreService;

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
    private PostHashtagCoreService postHashtagCoreService;

    @Mock
    private LikeCoreService likeCoreService;

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
            given(communityQueryService.getCommunity(anyLong()))
                    .willReturn(community);

            final Member member = TestEmptyEntityGenerator.Member();
            ReflectionTestUtils.setField(member, "id", 1L);
            ReflectionTestUtils.setField(member, "community", community);

            given(memberRepository.findByUserIdAndCommunityId(anyLong(), anyLong()))
                    .willReturn(Optional.of(member));

            given(postMediaQueryService.getUnmappedPostMediasByUUID(anyList()))
                    .willReturn(List.of());

            final Post post = TestEmptyEntityGenerator.Post();
            ReflectionTestUtils.setField(post, "id", 1L);
            ReflectionTestUtils.setField(post, "community", community);
            given(postRepository.save(any(Post.class)))
                    .willReturn(post);

            CreatePost createPost = new CreatePost(community.getId(), "내용", List.of(), List.of(), List.of());
            Post newPost = postCoreService.createPost(createPost, 1L);

            assertThat(newPost).isEqualTo(post);
        }
    }

    @Nested
    @DisplayName("글 상세 조회할시")
    class GetPostDetailTest {

        @Test
        @DisplayName("글이 작성된 공개 커뮤니티에 비가입상태로 조회시 성공한다.")
        void notJoinedUserRequestSuccess() {
            final User user = TestEmptyEntityGenerator.User();
            ReflectionTestUtils.setField(user, "id", 1L);

            final Community community = TestEmptyEntityGenerator.Community();
            ReflectionTestUtils.setField(community, "id", 1L);

            final Member member = TestEmptyEntityGenerator.Member();
            ReflectionTestUtils.setField(member, "id", 1L);
            ReflectionTestUtils.setField(member, "community", community);
            ReflectionTestUtils.setField(member, "user", user);

            final Post post = TestEmptyEntityGenerator.Post();
            ReflectionTestUtils.setField(post, "id", 1L);
            ReflectionTestUtils.setField(post, "content", "글");
            ReflectionTestUtils.setField(post, "member", member);
            ReflectionTestUtils.setField(post, "community", community);
            ReflectionTestUtils.setField(post, "likeCount", 0);
            ReflectionTestUtils.setField(post, "commentCount", 0);
            ReflectionTestUtils.setField(post, "hashtags", List.of());
            ReflectionTestUtils.setField(post, "createdAt", LocalDateTime.now());

            given(postRepository.getPostWithUserAndMemberAndCommunityByPostId(anyLong()))
                    .willReturn(Optional.of(post));

            given(memberRepository.findByUserIdAndCommunityId(anyLong(), anyLong()))
                    .willReturn(Optional.of(member));

            given(postMediaRepository.findByPostId(anyLong()))
                    .willReturn(List.of());

            PostDetail postDetail = postCoreService.getPostDetail(post.getId(), 1L);

            assertThat(postDetail.getId()).isEqualTo(post.getId());
            assertThat(postDetail.getUser().getId()).isEqualTo(user.getId());
            assertThat(postDetail.getMember().getId()).isEqualTo(member.getId());
            assertThat(postDetail.getCommunity().getId()).isEqualTo(community.getId());
            assertThat(postDetail.getLikeId()).isNull();
            assertThat(postDetail.getCreatedAt()).isEqualTo(post.getCreatedAt());
            assertThat(postDetail.getContent()).isEqualTo(post.getContent());
            assertThat(postDetail.getLikeCount()).isEqualTo(0);
            assertThat(postDetail.getCommentCount()).isEqualTo(0);
            assertThat(postDetail.getMe()).isTrue();
        }

        @Test
        @DisplayName("글이 작성된 공개 커뮤니티에 가입상태로 조회시 성공한다.")
        void joinedUserRequestSuccess() {
            final User user = TestEmptyEntityGenerator.User();
            ReflectionTestUtils.setField(user, "id", 1L);

            final Community community = TestEmptyEntityGenerator.Community();
            ReflectionTestUtils.setField(community, "id", 1L);

            final Member member = TestEmptyEntityGenerator.Member();
            ReflectionTestUtils.setField(member, "id", 1L);
            ReflectionTestUtils.setField(member, "community", community);
            ReflectionTestUtils.setField(member, "user", user);

            final Post post = TestEmptyEntityGenerator.Post();
            ReflectionTestUtils.setField(post, "id", 1L);
            ReflectionTestUtils.setField(post, "content", "글");
            ReflectionTestUtils.setField(post, "member", member);
            ReflectionTestUtils.setField(post, "community", community);
            ReflectionTestUtils.setField(post, "likeCount", 1);
            ReflectionTestUtils.setField(post, "commentCount", 0);
            ReflectionTestUtils.setField(post, "hashtags", List.of());
            ReflectionTestUtils.setField(post, "createdAt", LocalDateTime.now());

            given(postRepository.getPostWithUserAndMemberAndCommunityByPostId(anyLong()))
                    .willReturn(Optional.of(post));

            given(memberRepository.findByUserIdAndCommunityId(anyLong(), anyLong()))
                    .willReturn(Optional.of(member));


            final PostMedia postMedia = TestEmptyEntityGenerator.PostMedia();
            ReflectionTestUtils.setField(postMedia, "id", 1L);
            ReflectionTestUtils.setField(postMedia, "mediaURL", "url");
            ReflectionTestUtils.setField(postMedia, "mediaType", MediaType.IMG);
            given(postMediaRepository.findByPostId(anyLong()))
                    .willReturn(List.of(postMedia));

            final Like like = TestEmptyEntityGenerator.Like();
            ReflectionTestUtils.setField(like, "id", 1L);

            given(likeRepository.findPostLikeByPostIdAndMemberId(anyLong(), anyLong()))
                    .willReturn(Optional.of(like));

            PostDetail postDetail = postCoreService.getPostDetail(post.getId(), 1L);

            assertThat(postDetail.getId()).isEqualTo(post.getId());
            assertThat(postDetail.getUser().getId()).isEqualTo(user.getId());
            assertThat(postDetail.getMember().getId()).isEqualTo(member.getId());
            assertThat(postDetail.getCommunity().getId()).isEqualTo(community.getId());
            assertThat(postDetail.getPostMedias().size()).isEqualTo(1);
            assertThat(postDetail.getPostMedias().get(0).getType()).isEqualTo(postMedia.getMediaType());
            assertThat(postDetail.getPostMedias().get(0).getUrl()).isEqualTo(postMedia.getMediaURL());
            assertThat(postDetail.getLikeId()).isEqualTo(like.getId());
            assertThat(postDetail.getCreatedAt()).isEqualTo(post.getCreatedAt());
            assertThat(postDetail.getContent()).isEqualTo(post.getContent());
            assertThat(postDetail.getLikeCount()).isEqualTo(1);
            assertThat(postDetail.getCommentCount()).isEqualTo(0);
            assertThat(postDetail.getMe()).isTrue();
        }

        @Test
        @DisplayName("글이 작성된 비공개 커뮤니티에 비가입상태로 조회시 NotJoinedMemberException 발생한다.")
        void notJoinedPrivateCommunityFail() {
            final Community community = TestEmptyEntityGenerator.Community();
            ReflectionTestUtils.setField(community, "id", 1L);
            ReflectionTestUtils.setField(community, "isPrivate", true);

            final Post post = TestEmptyEntityGenerator.Post();
            ReflectionTestUtils.setField(post, "id", 1L);
            ReflectionTestUtils.setField(post, "community", community);

            given(postRepository.getPostWithUserAndMemberAndCommunityByPostId(anyLong()))
                    .willReturn(Optional.of(post));

            given(memberRepository.findByUserIdAndCommunityId(anyLong(), anyLong()))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> postCoreService.getPostDetail(post.getId(), 1L))
                    .isInstanceOf(NotJoinedMemberException.class);
        }
    }

    @Nested
    @DisplayName("글 수정시")
    class UpdatePostTest {

        @Test
        @DisplayName("글 작성자 본인이 아닌 유저가 요청하는 경우 NotAuthorizedException 발생한다.")
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

            given(postRepository.findPostById(anyLong()))
                    .willReturn(Optional.of(post));

            given(communityRepository.findCommunityById(anyLong()))
                    .willReturn(Optional.of(community));

            UpdatePost updatePost = new UpdatePost("글", List.of(), List.of());

            assertThatThrownBy(() -> postCoreService.updatePost(updatePost, post.getId(), 2L))
                    .isInstanceOf(NotAuthorizedMemberException.class);
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

            given(communityRepository.findCommunityById(anyLong()))
                    .willReturn(Optional.of(community));

            final Post post = TestEmptyEntityGenerator.Post();
            ReflectionTestUtils.setField(post, "id", 1L);
            ReflectionTestUtils.setField(post, "community", community);
            ReflectionTestUtils.setField(post, "member", member);
            ReflectionTestUtils.setField(post, "content", "글");

            given(postRepository.findPostById(anyLong()))
                    .willReturn(Optional.of(post));

            final PostHashtag postHashtag = TestEmptyEntityGenerator.PostHashtag();
            ReflectionTestUtils.setField(postHashtag, "id", 1L);
            ReflectionTestUtils.setField(postHashtag, "post", post);
            ReflectionTestUtils.setField(postHashtag, "tag", "해시태그");

            given(postHashtagCoreService.addTags(anyLong(), anyList()))
                    .willReturn(List.of(postHashtag));

            final PostMedia postMedia = TestEmptyEntityGenerator.PostMedia();
            ReflectionTestUtils.setField(postMedia, "id", 1L);
            ReflectionTestUtils.setField(postMedia, "uuid", "uuid");

            given(postMediaRepository.findByPostId(anyLong()))
                    .willReturn(List.of());

            given(postMediaQueryService.getUnmappedPostMediasByUUID(anyList()))
                    .willReturn(List.of(postMedia));

            UpdatePost updatePost = new UpdatePost("수정글", List.of(postHashtag.getTag()), List.of(postMedia.getUuid()));

            Post updatedPost = postCoreService.updatePost(updatePost, post.getId(), 1L);

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

            given(commentRepository.findAllByPostId(anyLong()))
                    .willReturn(List.of(comment));

            final PostMedia postMedia = TestEmptyEntityGenerator.PostMedia();
            ReflectionTestUtils.setField(postMedia, "id", 1L);

            List<PostMedia> postMedias = List.of(postMedia);
            given(postMediaRepository.findByPostId(anyLong()))
                    .willReturn(postMedias);

            postCoreService.deletePost(post.getId(), 1L);

            verify(postHashtagCoreService, times(1)).removeTagsByPostId(post.getId());
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

            given(memberValidationService.hasAuthWithoutThrow(anyLong(), anyLong(), any(MemberType.class)))
                    .willReturn(false);

            assertThatThrownBy(() -> postCoreService.deletePost(post.getId(), 2L))
                    .isInstanceOf(NotAuthorizedMemberException.class);
        }
    }

    @Nested
    @DisplayName("유저가 작성한 게시글들 조회시")
    class GetUserPostsTest {

        @Test
        @DisplayName("본인 세션으로 본인의 작성글들을 요청한 경우 본인의 작성글을 페이지네이션해서 조회한다.")
        void myPostRequestWithMySessionUserSuccess() {
            final User user = TestEmptyEntityGenerator.User();
            ReflectionTestUtils.setField(user, "id", 1L);

            final Community community = TestEmptyEntityGenerator.Community();
            ReflectionTestUtils.setField(community, "id", 1L);

            final Member member1 = TestEmptyEntityGenerator.Member();
            ReflectionTestUtils.setField(member1, "id", 1L);
            ReflectionTestUtils.setField(member1, "user", user);

            final Member member2 = TestEmptyEntityGenerator.Member();
            ReflectionTestUtils.setField(member2, "id", 2L);
            ReflectionTestUtils.setField(member2, "user", user);


            LocalDateTime now = LocalDateTime.now();

            final Post post1 = TestEmptyEntityGenerator.Post();
            ReflectionTestUtils.setField(post1, "id", 1L);
            ReflectionTestUtils.setField(post1, "community", community);
            ReflectionTestUtils.setField(post1, "member", member1);
            ReflectionTestUtils.setField(post1, "createdAt", now);

            final Post post2 = TestEmptyEntityGenerator.Post();
            ReflectionTestUtils.setField(post2, "id", 2L);
            ReflectionTestUtils.setField(post2, "community", community);
            ReflectionTestUtils.setField(post2, "member", member2);
            ReflectionTestUtils.setField(post2, "createdAt", now);

            List<Post> posts = List.of(post1, post2);
            given(memberRepository.findMemberIdsForQueryUserPostBySessionUserId(anyLong()))
                    .willReturn(List.of(member1.getId(), member2.getId()));

            Pageable pageable = PageRequest.of(0, 2);
            Page<Post> postPage = PageableExecutionUtils.getPage(posts, pageable, () -> posts.size());

            given(postRepository.getUserPostPageByMemberIds(anyList(), any(Pageable.class)))
                    .willReturn(postPage);

            UserPostPage userPostPage = postCoreService.getUserPosts(user.getId(), 1L, pageable);

            List<UserPostsDto> userPosts = userPostPage.getPosts();
            assertThat(userPosts.size()).isEqualTo(2);
            assertThat(userPosts.get(0).getId()).isEqualTo(post1.getId());
            assertThat(userPosts.get(0).getCommunity().getId()).isEqualTo(community.getId());
            assertThat(userPosts.get(0).getCreatedAt()).isEqualTo(now.toString());
            assertThat(userPosts.get(1).getId()).isEqualTo(post2.getId());
            assertThat(userPosts.get(1).getCommunity().getId()).isEqualTo(community.getId());
            assertThat(userPosts.get(1).getCreatedAt()).isEqualTo(now.toString());

            PaginationDto pageInfo = userPostPage.getPageInfo();
            assertThat(pageInfo.getNextPage()).isEqualTo(1);
            assertThat(pageInfo.isHasNext()).isFalse();
        }

        @Test
        @DisplayName("세션 유저와 요청한 유저가 다른 경우 세션 유저가 비가입된 비공개 커뮤니티의 글을 제외하고 페이지네이션해서 조회한다.")
        void othersPostRequestWithMySessionUserSuccess() {
            final User user1 = TestEmptyEntityGenerator.User();
            ReflectionTestUtils.setField(user1, "id", 1L);

            final Community community = TestEmptyEntityGenerator.Community();
            ReflectionTestUtils.setField(community, "id", 1L);
            ReflectionTestUtils.setField(community, "isPrivate", false);

            final Community pCommunity = TestEmptyEntityGenerator.Community();
            ReflectionTestUtils.setField(pCommunity, "id", 2L);
            ReflectionTestUtils.setField(pCommunity, "isPrivate", true);

            final Member member1 = TestEmptyEntityGenerator.Member();
            ReflectionTestUtils.setField(member1, "id", 1L);
            ReflectionTestUtils.setField(member1, "user", user1);
            ReflectionTestUtils.setField(member1, "community", pCommunity);

            final Member member2 = TestEmptyEntityGenerator.Member();
            ReflectionTestUtils.setField(member2, "id", 1L);
            ReflectionTestUtils.setField(member2, "user", user1);
            ReflectionTestUtils.setField(member2, "community", community);

            LocalDateTime now = LocalDateTime.now();

            final Post post1 = TestEmptyEntityGenerator.Post();
            ReflectionTestUtils.setField(post1, "id", 1L);
            ReflectionTestUtils.setField(post1, "community", pCommunity);
            ReflectionTestUtils.setField(post1, "member", member1);
            ReflectionTestUtils.setField(post1, "createdAt", now);

            final Post post2 = TestEmptyEntityGenerator.Post();
            ReflectionTestUtils.setField(post2, "id", 2L);
            ReflectionTestUtils.setField(post2, "community", community);
            ReflectionTestUtils.setField(post2, "member", member2);
            ReflectionTestUtils.setField(post2, "createdAt", now);

            given(userRepository.findUserById(anyLong()))
                    .willReturn(Optional.of(user1));

            List<Post> posts = List.of(post2);
            given(memberRepository.findMemberIdsForQueryUserPostByUserIdAndSessionUserId(anyLong(), anyLong()))
                    .willReturn(List.of(member2.getId()));

            Pageable pageable = PageRequest.of(0, 2);
            Page<Post> postPage = PageableExecutionUtils.getPage(posts, pageable, () -> posts.size());

            given(postRepository.getUserPostPageByMemberIds(anyList(), any(Pageable.class)))
                    .willReturn(postPage);

            UserPostPage userPostPage = postCoreService.getUserPosts(user1.getId(), 2L, pageable);

            List<UserPostsDto> userPosts = userPostPage.getPosts();
            assertThat(userPosts.size()).isEqualTo(1);
            assertThat(userPosts.get(0).getId()).isEqualTo(post2.getId());
            assertThat(userPosts.get(0).getCommunity().getId()).isEqualTo(community.getId());
            assertThat(userPosts.get(0).getCreatedAt()).isEqualTo(now.toString());

            PaginationDto pageInfo = userPostPage.getPageInfo();
            assertThat(pageInfo.getNextPage()).isEqualTo(1);
            assertThat(pageInfo.isHasNext()).isFalse();
        }
    }
}