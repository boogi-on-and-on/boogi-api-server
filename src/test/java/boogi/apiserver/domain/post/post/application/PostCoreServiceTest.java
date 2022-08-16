package boogi.apiserver.domain.post.post.application;

import boogi.apiserver.domain.comment.dao.CommentRepository;
import boogi.apiserver.domain.comment.domain.Comment;
import boogi.apiserver.domain.community.community.application.CommunityValidationService;
import boogi.apiserver.domain.community.community.dao.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
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
import boogi.apiserver.domain.post.post.dto.*;
import boogi.apiserver.domain.post.postmedia.application.PostMediaQueryService;
import boogi.apiserver.domain.post.postmedia.dao.PostMediaRepository;
import boogi.apiserver.domain.post.postmedia.domain.MediaType;
import boogi.apiserver.domain.post.postmedia.domain.PostMedia;
import boogi.apiserver.domain.user.dao.UserRepository;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.global.dto.PagnationDto;
import boogi.apiserver.global.webclient.push.SendPushNotification;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;


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
            given(communityValidationService.checkExistsCommunity(anyLong()))
                    .willReturn(community);

            Member member = Member.builder()
                    .id(1L)
                    .community(community)
                    .build();
            given(memberValidationService.checkMemberJoinedCommunity(anyLong(), anyLong()))
                    .willReturn(member);

            given(postMediaQueryService.getUnmappedPostMediasByUUID(anyList()))
                    .willReturn(List.of());

            Post post = Post.builder()
                    .id(1L)
                    .community(community)
                    .build();
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
            User user = User.builder()
                    .id(1L)
                    .build();

            Community community = Community.builder()
                    .id(1L)
                    .build();

            Member member = Member.builder()
                    .id(1L)
                    .user(user)
                    .community(community)
                    .build();

            Post post = Post.builder()
                    .id(1L)
                    .content("글")
                    .member(member)
                    .community(community)
                    .likeCount(0)
                    .commentCount(0)
                    .hashtags(List.of())
                    .build();
            post.setCreatedAt(LocalDateTime.now());
            given(postRepository.getPostWithUserAndMemberAndCommunityByPostId(anyLong()))
                    .willReturn(Optional.of(post));

            given(memberRepository.findByUserIdAndCommunityId(anyLong(), anyLong()))
                    .willReturn(List.of());

            given(communityValidationService.checkOnlyPrivateCommunity(anyLong()))
                    .willReturn(false);

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
            User user = User.builder()
                    .id(1L)
                    .build();

            Community community = Community.builder()
                    .id(1L)
                    .build();

            Member member = Member.builder()
                    .id(1L)
                    .user(user)
                    .community(community)
                    .build();

            Post post = Post.builder()
                    .id(1L)
                    .content("글")
                    .member(member)
                    .community(community)
                    .likeCount(1)
                    .commentCount(0)
                    .hashtags(List.of())
                    .build();
            post.setCreatedAt(LocalDateTime.now());
            given(postRepository.getPostWithUserAndMemberAndCommunityByPostId(anyLong()))
                    .willReturn(Optional.of(post));

            given(memberRepository.findByUserIdAndCommunityId(anyLong(), anyLong()))
                    .willReturn(List.of(member));

            given(communityValidationService.checkOnlyPrivateCommunity(anyLong()))
                    .willReturn(false);

            PostMedia postMedia = PostMedia.builder()
                    .id(1L)
                    .mediaType(MediaType.IMG)
                    .mediaURL("url")
                    .build();
            given(postMediaRepository.findByPostId(anyLong()))
                    .willReturn(List.of(postMedia));

            Like like = Like.builder()
                    .id(1L)
                    .build();
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
            Community community = Community.builder()
                    .id(1L)
                    .build();

            Post post = Post.builder()
                    .id(1L)
                    .community(community)
                    .build();
            given(postRepository.getPostWithUserAndMemberAndCommunityByPostId(anyLong()))
                    .willReturn(Optional.of(post));

            given(memberRepository.findByUserIdAndCommunityId(anyLong(), anyLong()))
                    .willReturn(List.of());

            given(communityValidationService.checkOnlyPrivateCommunity(anyLong()))
                    .willReturn(true);

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
            User user = User.builder()
                    .id(1L)
                    .build();

            Member member = Member.builder()
                    .id(1L)
                    .user(user)
                    .build();

            Community community = Community.builder()
                    .id(1L)
                    .build();

            Post post = Post.builder()
                    .id(1L)
                    .member(member)
                    .community(community)
                    .build();
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
            User user = User.builder()
                    .id(1L)
                    .build();

            Member member = Member.builder()
                    .id(1L)
                    .user(user)
                    .build();

            Community community = Community.builder()
                    .id(1L)
                    .build();
            given(communityRepository.findCommunityById(anyLong()))
                    .willReturn(Optional.of(community));

            Post post = Post.builder()
                    .id(1L)
                    .member(member)
                    .community(community)
                    .content("글")
                    .postMedias(new ArrayList<>())
                    .hashtags(new ArrayList<>())
                    .build();
            given(postRepository.findPostById(anyLong()))
                    .willReturn(Optional.of(post));

            PostHashtag postHashtag = PostHashtag.builder()
                    .id(1L)
                    .tag("해시태그")
                    .post(post)
                    .build();
            given(postHashtagCoreService.addTags(anyLong(), anyList()))
                    .willReturn(List.of(postHashtag));
            PostMedia postMedia = PostMedia.builder()
                    .id(1L)
                    .uuid("uuid")
                    .build();
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
            User user = User.builder()
                    .id(1L)
                    .build();

            Member member = Member.builder()
                    .id(1L)
                    .user(user)
                    .build();

            Community community = Community.builder()
                    .id(1L)
                    .build();

            Post post = Post.builder()
                    .id(1L)
                    .member(member)
                    .community(community)
                    .commentCount(1)
                    .build();
            given(postRepository.getPostWithCommunityAndMemberByPostId(anyLong()))
                    .willReturn(Optional.of(post));

            Comment comment = Comment.builder()
                    .id(1L)
                    .post(post)
                    .build();
            given(commentRepository.findAllByPostId(anyLong()))
                    .willReturn(List.of(comment));

            PostMedia postMedia = PostMedia.builder()
                    .id(1L)
                    .build();
            given(postMediaRepository.findByPostId(anyLong()))
                    .willReturn(List.of(postMedia));

            postCoreService.deletePost(post.getId(), 1L);

            assertThat(comment.getDeletedAt()).isNotNull();
            assertThat(post.getDeletedAt()).isNotNull();
            assertThat(postMedia.getDeletedAt()).isNotNull();
        }

        @Test
        @DisplayName("글 작성자가 본인이 아니거나, 해당 커뮤니티 (부)매니저가 아닐 경우 NotAuthorizedMemberException 발생한다.")
        void notAuthorizedMemberFail() {
            User user = User.builder()
                    .id(1L)
                    .build();

            Member member = Member.builder()
                    .id(1L)
                    .user(user)
                    .build();

            Community community = Community.builder()
                    .id(1L)
                    .build();

            Post post = Post.builder()
                    .id(1L)
                    .member(member)
                    .community(community)
                    .build();
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
            User user = User.builder()
                    .id(1L)
                    .build();

            Community community = Community.builder()
                    .id(1L)
                    .build();

            Member member1 = Member.builder()
                    .id(1L)
                    .user(user)
                    .build();
            Member member2 = Member.builder()
                    .id(2L)
                    .user(user)
                    .build();

            LocalDateTime now = LocalDateTime.now();
            Post post1 = Post.builder()
                    .id(1L)
                    .community(community)
                    .member(member1)
                    .hashtags(List.of())
                    .build();
            post1.setCreatedAt(now);
            Post post2 = Post.builder()
                    .id(2L)
                    .community(community)
                    .member(member2)
                    .hashtags(List.of())
                    .build();
            post2.setCreatedAt(now);
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

            PagnationDto pageInfo = userPostPage.getPageInfo();
            assertThat(pageInfo.getNextPage()).isEqualTo(1);
            assertThat(pageInfo.getTotalCount()).isEqualTo(2);
            assertThat(pageInfo.isHasNext()).isFalse();
        }

        @Test
        @DisplayName("세션 유저와 요청한 유저가 다른 경우 세션 유저가 비가입된 비공개 커뮤니티의 글을 제외하고 페이지네이션해서 조회한다.")
        void othersPostRequestWithMySessionUserSuccess() {
            User user1 = User.builder()
                    .id(1L)
                    .build();

            Community community = Community.builder()
                    .id(1L)
                    .isPrivate(false)
                    .build();
            Community pCommunity = Community.builder()
                    .id(2L)
                    .isPrivate(true)
                    .build();

            Member member1 = Member.builder()
                    .id(1L)
                    .user(user1)
                    .community(pCommunity)
                    .build();
            Member member2 = Member.builder()
                    .id(2L)
                    .user(user1)
                    .community(community)
                    .build();

            LocalDateTime now = LocalDateTime.now();
            Post post1 = Post.builder()
                    .id(1L)
                    .community(pCommunity)
                    .member(member1)
                    .hashtags(List.of())
                    .build();
            post1.setCreatedAt(now);
            Post post2 = Post.builder()
                    .id(2L)
                    .community(community)
                    .member(member2)
                    .hashtags(List.of())
                    .build();
            post2.setCreatedAt(now);
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

            PagnationDto pageInfo = userPostPage.getPageInfo();
            assertThat(pageInfo.getNextPage()).isEqualTo(1);
            assertThat(pageInfo.getTotalCount()).isEqualTo(1);
            assertThat(pageInfo.isHasNext()).isFalse();
        }
    }
}