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
import boogi.apiserver.domain.post.post.application.PostCoreService;
import boogi.apiserver.domain.post.post.application.PostQueryService;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.post.post.dto.enums.PostListingOrder;
import boogi.apiserver.domain.post.post.dto.request.CreatePost;
import boogi.apiserver.domain.post.post.dto.request.UpdatePost;
import boogi.apiserver.domain.post.post.dto.response.*;
import boogi.apiserver.domain.post.postmedia.domain.PostMedia;
import boogi.apiserver.domain.post.postmedia.dto.response.PostMediaMetadataDto;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.domain.user.dto.response.UserBasicProfileDto;
import boogi.apiserver.global.constant.HeaderConst;
import boogi.apiserver.global.constant.SessionInfoConst;
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
import org.springframework.data.domain.*;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(controllers = PostApiController.class)
class PostApiControllerTest {

    @MockBean
    private PostQueryService postQueryService;

    @MockBean
    PostCoreService postCoreService;

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

    @Test
    @DisplayName("글 생성")
    void testCreatePost() throws Exception {
        CreatePost createPost = new CreatePost(1L, "글", List.of(), List.of(), List.of());

        final Post post = TestEmptyEntityGenerator.Post();
        ReflectionTestUtils.setField(post, "id", 1L);

        given(postCoreService.createPost(any(CreatePost.class), anyLong()))
                .willReturn(post);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionInfoConst.USER_ID, 1L);

        mvc.perform(
                        MockMvcRequestBuilders.post("/api/posts/")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsBytes(createPost))
                                .session(session)
                                .header(HeaderConst.AUTH_TOKEN, "AUTO_TOKEN"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(post.getId()));
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

        final Member member = TestEmptyEntityGenerator.Member();
        ReflectionTestUtils.setField(member, "id", 1L);
        ReflectionTestUtils.setField(member, "user", user);
        ReflectionTestUtils.setField(member, "memberType", MemberType.MANAGER);

        final Community community = TestEmptyEntityGenerator.Community();
        ReflectionTestUtils.setField(community, "id", 1L);
        ReflectionTestUtils.setField(community, "communityName", "커뮤니티");


        final PostHashtag postHashtag = TestEmptyEntityGenerator.PostHashtag();
        ReflectionTestUtils.setField(postHashtag, "id", 1L);
        ReflectionTestUtils.setField(postHashtag, "tag", "해시태그");

        final Post post = TestEmptyEntityGenerator.Post();
        ReflectionTestUtils.setField(post, "id", 1L);
        ReflectionTestUtils.setField(post, "member", member);
        ReflectionTestUtils.setField(post, "community", community);
        ReflectionTestUtils.setField(post, "content", "글");
        ReflectionTestUtils.setField(post, "likeCount", 1);
        ReflectionTestUtils.setField(post, "commentCount", 0);
        ReflectionTestUtils.setField(post, "hashtags", List.of(postHashtag));
        ReflectionTestUtils.setField(post, "createdAt", LocalDateTime.now());

        PostMedia postMedia = PostMedia.builder()
                .id(1L)
                .post(post)
                .mediaType(boogi.apiserver.domain.post.postmedia.domain.MediaType.IMG)
                .mediaURL("mediaUrl")
                .build();

        PostDetail postDetail = new PostDetail(post, List.of(postMedia), Boolean.TRUE, 1L);

        given(postCoreService.getPostDetail(anyLong(), anyLong()))
                .willReturn(postDetail);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionInfoConst.USER_ID, 1L);

        mvc.perform(
                        MockMvcRequestBuilders.get("/api/posts/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .session(session)
                                .header(HeaderConst.AUTH_TOKEN, "AUTO_TOKEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(post.getId()))
                .andExpect(jsonPath("$.user.id").value(user.getId()))
                .andExpect(jsonPath("$.user.name").value(user.getUsername()))
                .andExpect(jsonPath("$.user.tagNum").value(user.getTagNumber()))
                .andExpect(jsonPath("$.user.profileImageUrl").value(user.getProfileImageUrl()))
                .andExpect(jsonPath("$.member.id").value(member.getId()))
                .andExpect(jsonPath("$.member.memberType").value(member.getMemberType().toString()))
                .andExpect(jsonPath("$.community.id").value(community.getId()))
                .andExpect(jsonPath("$.community.name").value(community.getCommunityName()))
                .andExpect(jsonPath("$.postMedias").isArray())
                .andExpect(jsonPath("$.postMedias[0].type").value(postMedia.getMediaType().toString()))
                .andExpect(jsonPath("$.postMedias[0].url").value(postMedia.getMediaURL()))
                .andExpect(jsonPath("$.likeId").value(1L))
                .andExpect(jsonPath("$.createdAt").value(CustomDateTimeFormatter.toString(post.getCreatedAt(), TimePattern.BASIC_FORMAT)))
                .andExpect(jsonPath("$.content").value(post.getContent()))
                .andExpect(jsonPath("$.hashtags").isArray())
                .andExpect(jsonPath("$.hashtags[0]").value(postHashtag.getTag()))
                .andExpect(jsonPath("$.likeCount").value(post.getLikeCount()))
                .andExpect(jsonPath("$.commentCount").value(post.getCommentCount()))
                .andExpect(jsonPath("$.me").value(true));
    }

    @Test
    @DisplayName("글 수정")
    void testUpdatePost() throws Exception {
        UpdatePost updatePost = new UpdatePost("글 수정", List.of(), List.of());

        final Post post = TestEmptyEntityGenerator.Post();
        ReflectionTestUtils.setField(post, "id", 1L);

        given(postCoreService.updatePost(any(UpdatePost.class), anyLong(), anyLong()))
                .willReturn(post);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionInfoConst.USER_ID, 1L);

        mvc.perform(
                        MockMvcRequestBuilders.patch("/api/posts/" + post.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsBytes(updatePost))
                                .session(session)
                                .header(HeaderConst.AUTH_TOKEN, "AUTO_TOKEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(post.getId()));
    }

    @Test
    @DisplayName("글 삭제")
    void testDeletePost() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionInfoConst.USER_ID, 1L);

        mvc.perform(
                        MockMvcRequestBuilders.delete("/api/posts/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .session(session)
                                .header(HeaderConst.AUTH_TOKEN, "AUTO_TOKEN"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("유저가 작성한 게시글을 페이지네이션해서 조회하기")
    void testGetUserPostsInfo() throws Exception {
        UserPostsDto postsDto = UserPostsDto.builder()
                .id(1L)
                .content("게시글 내용1")
                .community(UserPostsDto.CommunityDto.builder()
                        .id(1L)
                        .name("커뮤니티1")
                        .build())
                .build();

        UserPostPage pageInfo = UserPostPage.builder()
                .posts(List.of(postsDto))
                .pageInfo(PaginationDto.builder().nextPage(1).hasNext(false).build())
                .build();

        given(postCoreService.getUserPosts(anyLong(), anyLong(), any(Pageable.class)))
                .willReturn(pageInfo);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionInfoConst.USER_ID, 1L);

        mvc.perform(
                        MockMvcRequestBuilders.get("/api/posts/users")
                                .queryParam("page", "0")
                                .queryParam("size", "1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .session(session)
                                .header(HeaderConst.AUTH_TOKEN, "AUTO_TOKEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts.size()").value(1))
                .andExpect(jsonPath("$.posts[0].community.id").value("1"))
                .andExpect(jsonPath("$.posts[0].community.name").value("커뮤니티1"))
                .andExpect(jsonPath("$.posts[0].postMedias").doesNotExist())
                .andExpect(jsonPath("$.pageInfo.nextPage").value(1))
                .andExpect(jsonPath("$.pageInfo.hasNext").value(false));
    }

    @Test
    @DisplayName("글에 좋아요하기")
    void testDoLikeAtPost() throws Exception {
        final Like like = TestEmptyEntityGenerator.Like();
        ReflectionTestUtils.setField(like, "id", 1L);

        given(likeCoreService.doLikeAtPost(anyLong(), anyLong()))
                .willReturn(like);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionInfoConst.USER_ID, 1L);

        mvc.perform(
                        MockMvcRequestBuilders.post("/api/posts/1/likes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .session(session)
                                .header(HeaderConst.AUTH_TOKEN, "AUTO_TOKEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(like.getId()));
    }

    @Test
    @DisplayName("글에 좋아요 한 유저들 조회하기")
    void testGetLikeMembersAtPost() throws Exception {
        User user = User.builder()
                .id(1L)
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

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionInfoConst.USER_ID, 1L);

        mvc.perform(
                        MockMvcRequestBuilders.get("/api/posts/1/likes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .session(session)
                                .header(HeaderConst.AUTH_TOKEN, "AUTO_TOKEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.members").isArray())
                .andExpect(jsonPath("$.members.size()").value(1))
                .andExpect(jsonPath("$.members[0].id").value(user.getId()))
                .andExpect(jsonPath("$.members[0].name").value(user.getUsername()))
                .andExpect(jsonPath("$.members[0].tagNum").value(user.getTagNumber()))
                .andExpect(jsonPath("$.members[0].profileImageUrl").value(user.getProfileImageUrl()))
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
                .id(1L)
                .memberType(MemberType.MANAGER)
                .build();

        CommentsAtPost.ChildCommentInfo childCommentInfo = CommentsAtPost.ChildCommentInfo.builder()
                .id(2L)
                .content("자식댓글")
                .likeCount(0L)
                .parentId(1L)
                .user(userInfo)
                .member(memberInfo)
                .createdAt(LocalDateTime.now())
                .me(false)
                .build();
        List<CommentsAtPost.ChildCommentInfo> childCommentInfos = List.of(childCommentInfo);

        final Comment parentComment = TestEmptyEntityGenerator.Comment();
        ReflectionTestUtils.setField(parentComment, "id", 1L);
        ReflectionTestUtils.setField(parentComment, "content", "부모댓글");
        ReflectionTestUtils.setField(parentComment, "createdAt", LocalDateTime.now());

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
        Page<Comment> page = PageableExecutionUtils.getPage(comments, pageable, () -> comments.size());

        CommentsAtPost commentsAtPost = CommentsAtPost.of(parentCommentInfos, page);
        given(commentCoreService.getCommentsAtPost(anyLong(), anyLong(), any(Pageable.class)))
                .willReturn(commentsAtPost);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionInfoConst.USER_ID, 1L);

        mvc.perform(
                        MockMvcRequestBuilders.get("/api/posts/1/comments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .session(session)
                                .header(HeaderConst.AUTH_TOKEN, "AUTO_TOKEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comments").isArray())
                .andExpect(jsonPath("$.comments.size()").value(1))
                .andExpect(jsonPath("$.comments[0].id").value(parentComment.getId()))
                .andExpect(jsonPath("$.comments[0].user.id").value(userInfo.getId()))
                .andExpect(jsonPath("$.comments[0].user.name").value(userInfo.getName()))
                .andExpect(jsonPath("$.comments[0].user.tagNum").value(userInfo.getTagNum()))
                .andExpect(jsonPath("$.comments[0].user.profileImageUrl").value(userInfo.getProfileImageUrl()))
                .andExpect(jsonPath("$.comments[0].member.id").value(memberInfo.getId()))
                .andExpect(jsonPath("$.comments[0].member.memberType").value(memberInfo.getMemberType().toString()))
                .andExpect(jsonPath("$.comments[0].likeId").value(nullValue()))
                .andExpect(jsonPath("$.comments[0].createdAt").value(CustomDateTimeFormatter.toString(parentComment.getCreatedAt(), TimePattern.BASIC_FORMAT)))
                .andExpect(jsonPath("$.comments[0].content").value(parentComment.getContent()))
                .andExpect(jsonPath("$.comments[0].likeCount").value(0))
                .andExpect(jsonPath("$.comments[0].me").value(false))
                .andExpect(jsonPath("$.comments[0].child").isArray())
                .andExpect(jsonPath("$.comments[0].child.size()").value(1))
                .andExpect(jsonPath("$.comments[0].child[0].id").value(childCommentInfo.getId()))
                .andExpect(jsonPath("$.comments[0].child[0].user.id").value(userInfo.getId()))
                .andExpect(jsonPath("$.comments[0].child[0].user.name").value(userInfo.getName()))
                .andExpect(jsonPath("$.comments[0].child[0].user.tagNum").value(userInfo.getTagNum()))
                .andExpect(jsonPath("$.comments[0].child[0].user.profileImageUrl").value(userInfo.getProfileImageUrl()))
                .andExpect(jsonPath("$.comments[0].child[0].member.id").value(memberInfo.getId()))
                .andExpect(jsonPath("$.comments[0].child[0].member.memberType").value(memberInfo.getMemberType().toString()))
                .andExpect(jsonPath("$.comments[0].child[0].likeId").value(nullValue()))
                .andExpect(jsonPath("$.comments[0].child[0].createdAt").value(CustomDateTimeFormatter.toString(childCommentInfo.getCreatedAt(), TimePattern.BASIC_FORMAT)))
                .andExpect(jsonPath("$.comments[0].child[0].content").value(childCommentInfo.getContent()))
                .andExpect(jsonPath("$.comments[0].child[0].likeCount").value(0))
                .andExpect(jsonPath("$.comments[0].child[0].me").value(false))
                .andExpect(jsonPath("$.pageInfo.nextPage").value(1))
                .andExpect(jsonPath("$.pageInfo.hasNext").value(false));
    }

    @Test
    void 핫한게시물() throws Exception {
        HotPost hotPost1 = HotPost.builder()
                .postId(1L)
                .content("내용")
                .commentCount(1)
                .likeCount(1)
                .communityId(1L)
                .hashtags(List.of("hashtag1"))
                .build();

        HotPost hotPost2 = HotPost.builder()
                .postId(2L)
                .content("내용")
                .commentCount(2)
                .likeCount(2)
                .communityId(2L)
                .build();

        HotPost hotPost3 = HotPost.builder()
                .postId(3L)
                .content("내용")
                .commentCount(3)
                .communityId(3L)
                .likeCount(3)
                .build();

        given(postQueryService.getHotPosts())
                .willReturn(List.of(hotPost1, hotPost2, hotPost3));

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionInfoConst.USER_ID, 1L);

        mvc.perform(
                        MockMvcRequestBuilders.get("/api/posts/hot")
                                .contentType(MediaType.APPLICATION_JSON)
                                .session(session)
                                .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN"))
                .andExpect(jsonPath("$.hots.size()").value(3))
                .andExpect(jsonPath("$.hots[0].postId").isNumber())
                .andExpect(jsonPath("$.hots[0].content").isString())
                .andExpect(jsonPath("$.hots[0].commentCount").isNumber())
                .andExpect(jsonPath("$.hots[0].likeCount").isNumber())
                .andExpect(jsonPath("$.hots[0].communityId").isNumber())
                .andExpect(jsonPath("$.hots[0].hashtags").isArray());
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


        PageImpl page = new PageImpl(List.of(dto), Pageable.ofSize(1), 1);
        given(postQueryService.getSearchedPosts(any(), any(), anyLong()))
                .willReturn(page);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionInfoConst.USER_ID, 1L);

        mvc.perform(
                        MockMvcRequestBuilders.get("/api/posts/search")
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")
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