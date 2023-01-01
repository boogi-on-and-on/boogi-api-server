package boogi.apiserver.domain.post.post.api;

import boogi.apiserver.domain.comment.application.CommentCoreService;
import boogi.apiserver.domain.comment.domain.Comment;
import boogi.apiserver.domain.comment.dto.response.CommentsAtPost;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.hashtag.post.domain.PostHashtag;
import boogi.apiserver.domain.like.application.LikeCoreService;
import boogi.apiserver.domain.like.domain.Like;
import boogi.apiserver.domain.like.dto.response.LikeMembersAtPost;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.post.post.application.PostQueryService;
import boogi.apiserver.domain.post.post.application.PostService;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.post.post.dto.enums.PostListingOrder;
import boogi.apiserver.domain.post.post.dto.request.CreatePost;
import boogi.apiserver.domain.post.post.dto.request.UpdatePost;
import boogi.apiserver.domain.post.post.dto.response.*;
import boogi.apiserver.domain.post.postmedia.domain.MediaType;
import boogi.apiserver.domain.post.postmedia.domain.PostMedia;
import boogi.apiserver.domain.post.postmedia.dto.response.PostMediaMetadataDto;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.domain.user.dto.response.UserBasicProfileDto;
import boogi.apiserver.global.dto.PaginationDto;
import boogi.apiserver.global.util.PageableUtil;
import boogi.apiserver.global.util.time.CustomDateTimeFormatter;
import boogi.apiserver.global.util.time.TimePattern;
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
    LikeCoreService likeCoreService;

    @MockBean
    CommentCoreService commentCoreService;

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
        CreatePost createPost = new CreatePost(1L, "글", List.of(), List.of(), List.of());

        Post post = Post.builder()
                .id(2L)
                .build();

        given(postService.createPost(any(CreatePost.class), anyLong()))
                .willReturn(post);

        MockHttpSession session = createUserSession(3L);

        mvc.perform(
                        post("/api/posts/")
                                .contentType(APPLICATION_JSON)
                                .content(mapper.writeValueAsBytes(createPost))
                                .session(session)
                                .header(AUTH_TOKEN, "AUTO_TOKEN"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2L));
    }

    @Test
    @DisplayName("글 상세 조회하기")
    void testGetPostDetail() throws Exception {
        User user = User.builder()
                .id(1L)
                .username("유저")
                .tagNumber("#1")
                .profileImageUrl("url")
                .build();

        Member member = Member.builder()
                .id(2L)
                .user(user)
                .memberType(MemberType.MANAGER)
                .build();

        Community community = Community.builder()
                .id(3L)
                .communityName("커뮤니티")
                .build();

        PostHashtag postHashtag = PostHashtag.builder()
                .id(4L)
                .tag("해시태그")
                .build();

        Post post = Post.builder()
                .id(5L)
                .member(member)
                .community(community)
                .content("글")
                .likeCount(1)
                .commentCount(0)
                .hashtags(List.of(postHashtag))
                .build();
        post.setCreatedAt(LocalDateTime.now());

        PostMedia postMedia = PostMedia.builder()
                .id(6L)
                .post(post)
                .mediaType(MediaType.IMG)
                .mediaURL("mediaUrl")
                .build();

        MockHttpSession session = createUserSession(1L);

        PostDetail postDetail = new PostDetail(post, List.of(postMedia), 1L, 7L);

        given(postService.getPostDetail(anyLong(), anyLong()))
                .willReturn(postDetail);

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
        UpdatePost updatePost = new UpdatePost("글 수정", List.of(), List.of());

        Post post = Post.builder()
                .id(1L)
                .build();
        given(postService.updatePost(any(UpdatePost.class), anyLong(), anyLong()))
                .willReturn(post);

        MockHttpSession session = createUserSession(2L);

        mvc.perform(
                        patch("/api/posts/{postId}", post.getId())
                                .contentType(APPLICATION_JSON)
                                .content(mapper.writeValueAsBytes(updatePost))
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
        UserPostsDto postsDto = UserPostsDto.builder()
                .id(1L)
                .content("게시글 내용1")
                .community(UserPostsDto.CommunityDto.builder()
                        .id(2L)
                        .name("커뮤니티1")
                        .build())
                .build();

        UserPostPage pageInfo = UserPostPage.builder()
                .posts(List.of(postsDto))
                .pageInfo(PaginationDto.builder().nextPage(1).hasNext(false).build())
                .build();

        given(postService.getUserPosts(anyLong(), anyLong(), any(Pageable.class)))
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
        Like like = Like.builder()
                .id(2L)
                .build();
        given(likeCoreService.doLikeAtPost(anyLong(), anyLong()))
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
        User user = User.builder()
                .id(2L)
                .username("유저")
                .tagNumber("#1")
                .profileImageUrl("url")
                .build();
        List<User> users = List.of(user);

        Pageable pageable = PageRequest.of(0, 1);
        Slice<User> page = PageableUtil.getSlice(users, pageable);

        LikeMembersAtPost likeMembers = new LikeMembersAtPost(users, page);
        given(likeCoreService.getLikeMembersAtPost(anyLong(), anyLong(), any(Pageable.class)))
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
        CommentsAtPost.UserInfo userInfo = CommentsAtPost.UserInfo.builder()
                .id(1L)
                .name("유저")
                .tagNum("#1")
                .profileImageUrl("url")
                .build();

        CommentsAtPost.MemberInfo memberInfo = CommentsAtPost.MemberInfo.builder()
                .id(2L)
                .memberType(MemberType.MANAGER)
                .build();

        CommentsAtPost.ChildCommentInfo childCommentInfo = CommentsAtPost.ChildCommentInfo.builder()
                .id(4L)
                .content("자식댓글")
                .likeCount(0L)
                .parentId(3L)
                .user(userInfo)
                .member(memberInfo)
                .createdAt(LocalDateTime.now())
                .me(false)
                .build();
        List<CommentsAtPost.ChildCommentInfo> childCommentInfos = List.of(childCommentInfo);

        Comment parentComment = Comment.builder()
                .id(3L)
                .content("부모댓글")
                .build();
        parentComment.setCreatedAt(LocalDateTime.now());
        List<Comment> comments = List.of(parentComment);

        CommentsAtPost.ParentCommentInfo parentCommentInfo = CommentsAtPost.ParentCommentInfo.builder()
                .id(parentComment.getId())
                .content(parentComment.getContent())
                .likeCount(0L)
                .child(childCommentInfos)
                .me(false)
                .user(userInfo)
                .member(memberInfo)
                .createdAt(parentComment.getCreatedAt())
                .build();
        List<CommentsAtPost.ParentCommentInfo> parentCommentInfos = List.of(parentCommentInfo);

        Pageable pageable = PageRequest.of(0, 1);
        Slice<Comment> slice = PageableUtil.getSlice(comments, pageable);

        CommentsAtPost commentsAtPost = CommentsAtPost.of(parentCommentInfos, slice);
        given(commentCoreService.getCommentsAtPost(anyLong(), anyLong(), any(Pageable.class)))
                .willReturn(commentsAtPost);

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
        HotPost hotPost1 = HotPost.builder()
                .postId(1L)
                .content("내용1")
                .commentCount(1)
                .likeCount(1)
                .communityId(1L)
                .hashtags(List.of("hashtag1"))
                .build();

        HotPost hotPost2 = HotPost.builder()
                .postId(2L)
                .content("내용2")
                .commentCount(2)
                .likeCount(2)
                .communityId(2L)
                .build();

        HotPost hotPost3 = HotPost.builder()
                .postId(3L)
                .content("내용3")
                .commentCount(3)
                .communityId(3L)
                .likeCount(3)
                .build();

        given(postQueryService.getHotPosts())
                .willReturn(List.of(hotPost1, hotPost2, hotPost3));

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
        SearchPostDto dto = SearchPostDto.builder()
                .id(1L)
                .commentCount(1)
                .communityName("팍스")
                .createdAt(LocalDateTime.now().toString())
                .communityId(2L)
                .content("게시글내용")
                .hashtags(List.of("해시테그1", "해시태그2"))
                .postMedias(List.of(
                        PostMediaMetadataDto.builder()
                                .url("123")
                                .type("IMG")
                                .build(),
                        PostMediaMetadataDto.builder()
                                .url("456")
                                .type("IMG")
                                .build()
                ))
                .likeCount(2)
                .user(UserBasicProfileDto.builder()
                        .id(1L)
                        .tagNum("#0001")
                        .name("김")
                        .build())
                .build();

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