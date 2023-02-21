package boogi.apiserver.domain.post.post.api;

import boogi.apiserver.domain.comment.application.CommentService;
import boogi.apiserver.domain.comment.domain.Comment;
import boogi.apiserver.domain.comment.dto.response.CommentsAtPostResponse;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.hashtag.post.domain.PostHashtag;
import boogi.apiserver.domain.like.application.LikeService;
import boogi.apiserver.domain.like.domain.Like;
import boogi.apiserver.domain.like.dto.response.LikeMembersAtPostResponse;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.post.post.application.PostQueryService;
import boogi.apiserver.domain.post.post.application.PostService;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.post.post.dto.dto.HotPostDto;
import boogi.apiserver.domain.post.post.dto.dto.SearchPostDto;
import boogi.apiserver.domain.post.post.dto.dto.UserPostDto;
import boogi.apiserver.domain.post.post.dto.enums.PostListingOrder;
import boogi.apiserver.domain.post.post.dto.request.CreatePostRequest;
import boogi.apiserver.domain.post.post.dto.request.UpdatePostRequest;
import boogi.apiserver.domain.post.post.dto.response.HotPostsResponse;
import boogi.apiserver.domain.post.post.dto.response.PostDetailResponse;
import boogi.apiserver.domain.post.post.dto.response.UserPostPageResponse;
import boogi.apiserver.domain.post.postmedia.domain.MediaType;
import boogi.apiserver.domain.post.postmedia.domain.PostMedia;
import boogi.apiserver.domain.post.postmedia.dto.dto.PostMediaMetadataDto;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.domain.user.dto.dto.UserBasicProfileDto;
import boogi.apiserver.global.dto.PaginationDto;
import boogi.apiserver.global.util.PageableUtil;
import boogi.apiserver.global.util.time.CustomDateTimeFormatter;
import boogi.apiserver.global.util.time.TimePattern;
import boogi.apiserver.utils.TestEmptyEntityGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.time.LocalDateTime;
import java.util.List;

import static boogi.apiserver.global.constant.HeaderConst.AUTH_TOKEN;
import static boogi.apiserver.global.constant.SessionInfoConst.USER_ID;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(controllers = PostApiController.class)
class PostApiControllerTest {

    @MockBean
    private PostQueryService postQueryService;

    @MockBean
    PostService postService;

    @MockBean
    LikeService likeService;

    @MockBean
    CommentService commentService;

    private MockMvc mvc;

    @Autowired
    ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private WebApplicationContext ctx;

    @BeforeEach
    private void setup() {
        mvc =
                MockMvcBuilders.webAppContextSetup(ctx)
                        .addFilter(new CharacterEncodingFilter("UTF-8", true))
                        .alwaysDo(print())
                        .build();
    }

    private MockHttpSession createUserSession(Long sessionUserId) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(USER_ID, sessionUserId);
        return session;
    }

    @Test
    @DisplayName("글 생성")
    void testCreatePost() throws Exception {
        CreatePostRequest createPostRequest = new CreatePostRequest(1L, "글", List.of(), List.of(), List.of());

        final Post post = TestEmptyEntityGenerator.Post();
        ReflectionTestUtils.setField(post, "id", 2L);

        given(postService.createPost(any(CreatePostRequest.class), anyLong()))
                .willReturn(post);

        MockHttpSession session = createUserSession(3L);

        mvc.perform(
                        post("/api/posts/")
                                .contentType(APPLICATION_JSON)
                                .content(mapper.writeValueAsBytes(createPostRequest))
                                .session(session)
                                .header(AUTH_TOKEN, "AUTO_TOKEN"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2L));
    }

    @Test
    @DisplayName("글 상세 조회하기")
    void testGetPostDetail() throws Exception {
        final User user = TestEmptyEntityGenerator.User();
        ReflectionTestUtils.setField(user, "id", 1L);
        ReflectionTestUtils.setField(user, "username", "유저");
        ReflectionTestUtils.setField(user, "tagNumber", "#1");
        ReflectionTestUtils.setField(user, "profileImageUrl", "url");


        final Member member = TestEmptyEntityGenerator.Member();
        ReflectionTestUtils.setField(member, "id", 2L);
        ReflectionTestUtils.setField(member, "user", user);
        ReflectionTestUtils.setField(member, "memberType", MemberType.MANAGER);

        final Community community = TestEmptyEntityGenerator.Community();
        ReflectionTestUtils.setField(community, "id", 3L);
        ReflectionTestUtils.setField(community, "communityName", "커뮤니티");


        final PostHashtag postHashtag = TestEmptyEntityGenerator.PostHashtag();
        ReflectionTestUtils.setField(postHashtag, "id", 4L);
        ReflectionTestUtils.setField(postHashtag, "tag", "해시태그");

        final Post post = TestEmptyEntityGenerator.Post();
        ReflectionTestUtils.setField(post, "id", 5L);
        ReflectionTestUtils.setField(post, "member", member);
        ReflectionTestUtils.setField(post, "community", community);
        ReflectionTestUtils.setField(post, "content", "글");
        ReflectionTestUtils.setField(post, "likeCount", 1);
        ReflectionTestUtils.setField(post, "commentCount", 0);
        ReflectionTestUtils.setField(post, "hashtags", List.of(postHashtag));
        ReflectionTestUtils.setField(post, "createdAt", LocalDateTime.now());

        final PostMedia postMedia = TestEmptyEntityGenerator.PostMedia();
        ReflectionTestUtils.setField(postMedia, "id", 6L);
        ReflectionTestUtils.setField(postMedia, "post", post);
        ReflectionTestUtils.setField(postMedia, "mediaURL", "mediaUrl");
        ReflectionTestUtils.setField(postMedia, "mediaType", MediaType.IMG);

        MockHttpSession session = createUserSession(1L);

        PostDetailResponse postDetailResponse = PostDetailResponse.of(post, List.of(postMedia), 1L, 7L);

        given(postQueryService.getPostDetail(anyLong(), anyLong()))
                .willReturn(postDetailResponse);

        String formattedCreatedTime = CustomDateTimeFormatter
                .toString(post.getCreatedAt(), TimePattern.BASIC_FORMAT);

        mvc.perform(
                        get("/api/posts/{postId}", 5L)
                                .contentType(APPLICATION_JSON)
                                .session(session)
                                .header(AUTH_TOKEN, "AUTO_TOKEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.user.id").value(1L))
                .andExpect(jsonPath("$.user.name").value("유저"))
                .andExpect(jsonPath("$.user.tagNum").value("#1"))
                .andExpect(jsonPath("$.user.profileImageUrl").value("url"))
                .andExpect(jsonPath("$.member.id").value(2L))
                .andExpect(jsonPath("$.member.memberType").value("MANAGER"))
                .andExpect(jsonPath("$.community.id").value(3L))
                .andExpect(jsonPath("$.community.name").value("커뮤니티"))
                .andExpect(jsonPath("$.postMedias").isArray())
                .andExpect(jsonPath("$.postMedias[0].type").value("IMG"))
                .andExpect(jsonPath("$.postMedias[0].url").value("mediaUrl"))
                .andExpect(jsonPath("$.likeId").value(7L))
                .andExpect(jsonPath("$.createdAt").value(formattedCreatedTime))
                .andExpect(jsonPath("$.content").value("글"))
                .andExpect(jsonPath("$.hashtags").isArray())
                .andExpect(jsonPath("$.hashtags[0]").value("해시태그"))
                .andExpect(jsonPath("$.likeCount").value(1))
                .andExpect(jsonPath("$.commentCount").value(0))
                .andExpect(jsonPath("$.me").value(true));
    }

    @Test
    @DisplayName("글 수정")
    void testUpdatePost() throws Exception {
        UpdatePostRequest updatePostRequest = new UpdatePostRequest("글 수정", List.of(), List.of());

        final Post post = TestEmptyEntityGenerator.Post();
        ReflectionTestUtils.setField(post, "id", 1L);

        given(postService.updatePost(any(UpdatePostRequest.class), anyLong(), anyLong()))
                .willReturn(post);

        MockHttpSession session = createUserSession(2L);

        mvc.perform(
                        patch("/api/posts/{postId}", post.getId())
                                .contentType(APPLICATION_JSON)
                                .content(mapper.writeValueAsBytes(updatePostRequest))
                                .session(session)
                                .header(AUTH_TOKEN, "AUTO_TOKEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("글 삭제")
    void testDeletePost() throws Exception {
        MockHttpSession session = createUserSession(2L);

        mvc.perform(
                        delete("/api/posts/{postId}", 1L)
                                .contentType(APPLICATION_JSON)
                                .session(session)
                                .header(AUTH_TOKEN, "AUTO_TOKEN"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("유저가 작성한 게시글을 페이지네이션해서 조회하기")
    void testGetUserPostsInfo() throws Exception {
        UserPostDto.CommunityDto communityDto = new UserPostDto.CommunityDto(2L, "커뮤니티1");
        UserPostDto postsDto = new UserPostDto(1L, "게시글 내용1", communityDto, null, null, null);

        UserPostPageResponse pageInfo = new UserPostPageResponse(List.of(postsDto), new PaginationDto(1, false));

        given(postQueryService.getUserPosts(anyLong(), anyLong(), any(Pageable.class)))
                .willReturn(pageInfo);

        MockHttpSession session = createUserSession(3L);

        mvc.perform(
                        get("/api/posts/users")
                                .queryParam("page", "0")
                                .queryParam("size", "1")
                                .contentType(APPLICATION_JSON)
                                .session(session)
                                .header(AUTH_TOKEN, "AUTO_TOKEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts.size()").value(1))
                .andExpect(jsonPath("$.posts[0].community.id").value(2L))
                .andExpect(jsonPath("$.posts[0].community.name").value("커뮤니티1"))
                .andExpect(jsonPath("$.posts[0].postMedias").doesNotExist())
                .andExpect(jsonPath("$.pageInfo.nextPage").value(1))
                .andExpect(jsonPath("$.pageInfo.hasNext").value(false));
    }

    @Test
    @DisplayName("글에 좋아요하기")
    void testDoLikeAtPost() throws Exception {
        final Like like = TestEmptyEntityGenerator.Like();
        ReflectionTestUtils.setField(like, "id", 2L);

        given(likeService.doLikeAtPost(anyLong(), anyLong()))
                .willReturn(like);

        MockHttpSession session = createUserSession(3L);

        mvc.perform(
                        post("/api/posts/{postId}/likes", 1L)
                                .contentType(APPLICATION_JSON)
                                .session(session)
                                .header(AUTH_TOKEN, "AUTO_TOKEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2L));
    }

    @Test
    @DisplayName("글에 좋아요 한 유저들 조회하기")
    void testGetLikeMembersAtPost() throws Exception {
        final User user = TestEmptyEntityGenerator.User();
        ReflectionTestUtils.setField(user, "id", 2L);
        ReflectionTestUtils.setField(user, "username", "유저");
        ReflectionTestUtils.setField(user, "tagNumber", "#1");
        ReflectionTestUtils.setField(user, "profileImageUrl", "url");

        List<User> users = List.of(user);

        Pageable pageable = PageRequest.of(0, 1);
        Slice<User> page = PageableUtil.getSlice(users, pageable);

        LikeMembersAtPostResponse likeMembers = LikeMembersAtPostResponse.of(users, page);
        given(likeService.getLikeMembersAtPost(anyLong(), anyLong(), any(Pageable.class)))
                .willReturn(likeMembers);

        MockHttpSession session = createUserSession(3L);

        mvc.perform(
                        get("/api/posts/{postId}/likes", 1L)
                                .contentType(APPLICATION_JSON)
                                .session(session)
                                .header(AUTH_TOKEN, "AUTO_TOKEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.members").isArray())
                .andExpect(jsonPath("$.members.size()").value(1))
                .andExpect(jsonPath("$.members[0].id").value(2L))
                .andExpect(jsonPath("$.members[0].name").value("유저"))
                .andExpect(jsonPath("$.members[0].tagNum").value("#1"))
                .andExpect(jsonPath("$.members[0].profileImageUrl").value("url"))
                .andExpect(jsonPath("$.pageInfo.nextPage").value(1))
                .andExpect(jsonPath("$.pageInfo.hasNext").value(false));
    }

    @Test
    @DisplayName("글에 달린 댓글들 조회하기")
    void testGetCommentsAtPost() throws Exception {
        UserBasicProfileDto userInfo = new UserBasicProfileDto(1L, "url", "#1", "유저");

        CommentsAtPostResponse.MemberInfo memberInfo = new CommentsAtPostResponse.MemberInfo(2L, MemberType.MANAGER);

        CommentsAtPostResponse.ChildCommentInfo childCommentInfo =
                new CommentsAtPostResponse.ChildCommentInfo(4L, userInfo, memberInfo, null, LocalDateTime.now(),
                        "자식댓글", 0L, false, 3L);

        List<CommentsAtPostResponse.ChildCommentInfo> childCommentInfos = List.of(childCommentInfo);

        final Comment parentComment = TestEmptyEntityGenerator.Comment();
        ReflectionTestUtils.setField(parentComment, "id", 3L);
        ReflectionTestUtils.setField(parentComment, "content", "부모댓글");
        ReflectionTestUtils.setField(parentComment, "createdAt", LocalDateTime.now());

        List<Comment> comments = List.of(parentComment);

        CommentsAtPostResponse.ParentCommentInfo parentCommentInfo =
                new CommentsAtPostResponse.ParentCommentInfo(parentComment.getId(), userInfo, memberInfo, null,
                        parentComment.getCreatedAt(), parentComment.getContent(), 0L, false, childCommentInfos);

        List<CommentsAtPostResponse.ParentCommentInfo> parentCommentInfos = List.of(parentCommentInfo);

        Pageable pageable = PageRequest.of(0, 1);
        Slice<Comment> slice = PageableUtil.getSlice(comments, pageable);

        CommentsAtPostResponse commentsAtPostResponse = CommentsAtPostResponse.of(parentCommentInfos, slice);
        given(commentService.getCommentsAtPost(anyLong(), anyLong(), any(Pageable.class)))
                .willReturn(commentsAtPostResponse);

        MockHttpSession session = createUserSession(5L);

        String formattedParentCreatedAt = CustomDateTimeFormatter
                .toString(parentComment.getCreatedAt(), TimePattern.BASIC_FORMAT);

        String formattedChildCreatedAt = CustomDateTimeFormatter
                .toString(childCommentInfo.getCreatedAt(), TimePattern.BASIC_FORMAT);
        mvc.perform(
                        get("/api/posts/{postId}/comments", 6L)
                                .contentType(APPLICATION_JSON)
                                .session(session)
                                .header(AUTH_TOKEN, "AUTO_TOKEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comments").isArray())
                .andExpect(jsonPath("$.comments.size()").value(1))
                .andExpect(jsonPath("$.comments[0].id").value(3L))
                .andExpect(jsonPath("$.comments[0].user.id").value(1L))
                .andExpect(jsonPath("$.comments[0].user.name").value("유저"))
                .andExpect(jsonPath("$.comments[0].user.tagNum").value("#1"))
                .andExpect(jsonPath("$.comments[0].user.profileImageUrl").value("url"))
                .andExpect(jsonPath("$.comments[0].member.id").value(2L))
                .andExpect(jsonPath("$.comments[0].member.memberType").value("MANAGER"))
                .andExpect(jsonPath("$.comments[0].likeId").value(nullValue()))
                .andExpect(jsonPath("$.comments[0].createdAt").value(formattedParentCreatedAt))
                .andExpect(jsonPath("$.comments[0].content").value("부모댓글"))
                .andExpect(jsonPath("$.comments[0].likeCount").value(0))
                .andExpect(jsonPath("$.comments[0].me").value(false))
                .andExpect(jsonPath("$.comments[0].child").isArray())
                .andExpect(jsonPath("$.comments[0].child.size()").value(1))
                .andExpect(jsonPath("$.comments[0].child[0].id").value(4L))
                .andExpect(jsonPath("$.comments[0].child[0].user.id").value(1L))
                .andExpect(jsonPath("$.comments[0].child[0].user.name").value("유저"))
                .andExpect(jsonPath("$.comments[0].child[0].user.tagNum").value("#1"))
                .andExpect(jsonPath("$.comments[0].child[0].user.profileImageUrl").value("url"))
                .andExpect(jsonPath("$.comments[0].child[0].member.id").value(2L))
                .andExpect(jsonPath("$.comments[0].child[0].member.memberType").value("MANAGER"))
                .andExpect(jsonPath("$.comments[0].child[0].likeId").value(nullValue()))
                .andExpect(jsonPath("$.comments[0].child[0].createdAt").value(formattedChildCreatedAt))
                .andExpect(jsonPath("$.comments[0].child[0].content").value("자식댓글"))
                .andExpect(jsonPath("$.comments[0].child[0].likeCount").value(0))
                .andExpect(jsonPath("$.comments[0].child[0].me").value(false))
                .andExpect(jsonPath("$.pageInfo.nextPage").value(1))
                .andExpect(jsonPath("$.pageInfo.hasNext").value(false));
    }

    @Test
    void 핫한게시물() throws Exception {
        HotPostDto hotPostDto1 = new HotPostDto(1L, 1, 1, "내용1", 1L, List.of("hashtag1"));

        HotPostDto hotPostDto2 = new HotPostDto(2L, 2, 2, "내용2", 2L, null);

        HotPostDto hotPostDto3 = new HotPostDto(3L, 3, 3, "내용3", 3L, null);

        given(postQueryService.getHotPosts())
                .willReturn(HotPostsResponse.from(List.of(hotPostDto1, hotPostDto2, hotPostDto3)));

        MockHttpSession session = createUserSession(1L);

        mvc.perform(
                        get("/api/posts/hot")
                                .contentType(APPLICATION_JSON)
                                .session(session)
                                .header(AUTH_TOKEN, "AUTH_TOKEN"))
                .andExpect(jsonPath("$.hots.size()").value(3))
                .andExpect(jsonPath("$.hots[0].postId").value(1L))
                .andExpect(jsonPath("$.hots[0].content").value("내용1"))
                .andExpect(jsonPath("$.hots[0].commentCount").value(1))
                .andExpect(jsonPath("$.hots[0].likeCount").value(1))
                .andExpect(jsonPath("$.hots[0].communityId").value(1L))
                .andExpect(jsonPath("$.hots[0].hashtags").isArray())
                .andExpect(jsonPath("$.hots[0].hashtags[0]").value("hashtag1"));
    }

    @Test
    void 게시물_검색() throws Exception {
        UserBasicProfileDto user = new UserBasicProfileDto(1L, null, "#0001", "김");
        List<PostMediaMetadataDto> postMedias = List.of(
                new PostMediaMetadataDto("123", "IMG"),
                new PostMediaMetadataDto("456", "IMG")
        );
        List<String> hashtags = List.of("해시테그1", "해시태그2");

        SearchPostDto dto = new SearchPostDto(1L, user, 2L, "팍스", LocalDateTime.now(),
                hashtags, postMedias, 1, 2, "게시글내용");

        Slice<SearchPostDto> page = PageableUtil.getSlice(List.of(dto), Pageable.ofSize(1));
        given(postQueryService.getSearchedPosts(any(), any(), anyLong()))
                .willReturn(page);

        MockHttpSession session = createUserSession(1L);

        mvc.perform(
                        get("/api/posts/search")
                                .contentType(APPLICATION_FORM_URLENCODED_VALUE)
                                .header(AUTH_TOKEN, "AUTH_TOKEN")
                                .session(session)
                                .queryParam("page", "0")
                                .queryParam("size", "1")
                                .queryParam("keyword", "헤헤")
                                .queryParam("order", PostListingOrder.LIKE_UPPER.toString())

                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.posts").isArray())
                .andExpect(jsonPath("$.posts[0].id").value(1L))
                .andExpect(jsonPath("$.posts[0].commentCount").value(1))
                .andExpect(jsonPath("$.posts[0].communityName").value("팍스"))
                .andExpect(jsonPath("$.posts[0].communityId").value(2L))
                .andExpect(jsonPath("$.posts[0].content").value("게시글내용"))
                .andExpect(jsonPath("$.posts[0].likeCount").value(2))
                .andExpect(jsonPath("$.posts[0].hashtags").isArray())
                .andExpect(jsonPath("$.posts[0].user").isMap())
                .andExpect(jsonPath("$.posts[0].postMedias[0].size()").value(2))
                .andExpect(jsonPath("$.posts[0].postMedias[0].url").value("123"));
    }
}