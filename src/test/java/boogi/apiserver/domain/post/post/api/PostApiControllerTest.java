package boogi.apiserver.domain.post.post.api;

import boogi.apiserver.domain.comment.application.CommentQueryService;
import boogi.apiserver.domain.comment.dto.response.CommentsAtPostResponse;
import boogi.apiserver.domain.like.application.LikeCommandService;
import boogi.apiserver.domain.like.application.LikeQueryService;
import boogi.apiserver.domain.like.dto.response.LikeMembersAtPostResponse;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.post.post.application.PostCommandService;
import boogi.apiserver.domain.post.post.application.PostQueryService;
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
import boogi.apiserver.domain.post.postmedia.dto.dto.PostMediaMetadataDto;
import boogi.apiserver.domain.user.dto.dto.UserBasicProfileDto;
import boogi.apiserver.global.constant.HeaderConst;
import boogi.apiserver.global.dto.PaginationDto;
import boogi.apiserver.global.util.PageableUtil;
import boogi.apiserver.global.webclient.push.SendPushNotification;
import boogi.apiserver.utils.controller.TestControllerSetUp;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(controllers = PostApiController.class)
class PostApiControllerTest extends TestControllerSetUp {

    @MockBean
    PostQueryService postQueryService;

    @MockBean
    PostCommandService postCommandService;

    @MockBean
    LikeCommandService likeCommandService;

    @MockBean
    LikeQueryService likeQueryService;

    @MockBean
    CommentQueryService commentQueryService;

    @MockBean
    SendPushNotification sendPushNotification;

    @Test
    @DisplayName("글 생성")
    void testCreatePost() throws Exception {
        final Long NEW_POST_ID = 2L;
        CreatePostRequest request = new CreatePostRequest(1L, "글", List.of(), List.of(), List.of());

        given(postCommandService.createPost(any(CreatePostRequest.class), anyLong()))
                .willReturn(NEW_POST_ID);

        ResultActions result = mvc.perform(
                post("/api/posts/")
                        .contentType(APPLICATION_JSON)
                        .content(mapper.writeValueAsBytes(request))
                        .session(dummySession)
                        .header(HeaderConst.AUTH_TOKEN, TOKEN));

        result
                .andExpect(status().isCreated())
                .andDo(document("post/post",
                        requestFields(
                                fieldWithPath("communityId").type(JsonFieldType.NUMBER)
                                        .description("커뮤니티 ID"),
                                fieldWithPath("content").type(JsonFieldType.STRING)
                                        .description("게시글 내용"),
                                fieldWithPath("hashtags").type(JsonFieldType.ARRAY)
                                        .description("해시태그 목록"),
                                fieldWithPath("postMediaIds").type(JsonFieldType.ARRAY)
                                        .description("게시글 미디어 UUID 목록"),
                                fieldWithPath("mentionedUserIds").type(JsonFieldType.ARRAY)
                                        .description("멘션할 유저의 ID 목록")
                        ),
                        responseFields(
                                fieldWithPath("id").type(JsonFieldType.NUMBER)
                                        .description("게시글 ID")
                        )
                ));
    }

    @Test
    @DisplayName("글 상세 조회하기")
    void testGetPostDetail() throws Exception {
        UserBasicProfileDto userDto = new UserBasicProfileDto(1L, "url", "#0001", "유저");
        PostDetailResponse.MemberInfo memberDto = new PostDetailResponse.MemberInfo(1L, MemberType.NORMAL);
        PostDetailResponse.CommunityInfo communityDto = new PostDetailResponse.CommunityInfo(1L, "커뮤니티 이름");
        PostDetailResponse.PostMediaInfo postMediaDto = new PostDetailResponse.PostMediaInfo(MediaType.IMG, "media url");

        PostDetailResponse response =
                new PostDetailResponse(1L, userDto, memberDto, communityDto, List.of(postMediaDto), 1L,
                        LocalDateTime.now(), "내용", List.of("해시태그"), 1, 1, true);

        given(postQueryService.getPostDetail(anyLong(), anyLong()))
                .willReturn(response);

        ResultActions result = mvc.perform(
                get("/api/posts/{postId}", 1L)
                        .contentType(APPLICATION_JSON)
                        .session(dummySession)
                        .header(HeaderConst.AUTH_TOKEN, TOKEN));

        result
                .andExpect(status().isOk())
                .andDo(document("post/get-postId",
                        pathParameters(
                                parameterWithName("postId").description("게시글 ID")
                        ),
                        responseFields(
                                fieldWithPath("id").type(JsonFieldType.NUMBER)
                                        .description("게시글 ID"),
                                fieldWithPath("user").type(JsonFieldType.OBJECT)
                                        .description("유저 정보"),
                                fieldWithPath("user.id").type(JsonFieldType.NUMBER)
                                        .description("유저 ID"),
                                fieldWithPath("user.profileImageUrl").type(JsonFieldType.STRING)
                                        .description("프로필 이미지 주소").optional(),
                                fieldWithPath("user.tagNum").type(JsonFieldType.STRING)
                                        .description("태그 번호"),
                                fieldWithPath("user.name").type(JsonFieldType.STRING)
                                        .description("유저 이름"),
                                fieldWithPath("member").type(JsonFieldType.OBJECT)
                                        .description("멤버 정보"),
                                fieldWithPath("member.id").type(JsonFieldType.NUMBER)
                                        .description("멤버 ID"),
                                fieldWithPath("member.memberType").type(JsonFieldType.STRING)
                                        .description("멤버 타입으로 MANAGER, SUB_MANAGER, NORMAL 중 하나"),
                                fieldWithPath("community").type(JsonFieldType.OBJECT)
                                        .description("커뮤니티 정보"),
                                fieldWithPath("community.id").type(JsonFieldType.NUMBER)
                                        .description("커뮤니티 ID"),
                                fieldWithPath("community.name").type(JsonFieldType.STRING)
                                        .description("커뮤니티 이름"),
                                fieldWithPath("postMedias").type(JsonFieldType.ARRAY)
                                        .description("게시글 미디어 목록").optional(),
                                fieldWithPath("postMedias[].type").type(JsonFieldType.STRING)
                                        .description("게시글 미디어의 타입으로 IMG만 가능"),
                                fieldWithPath("postMedias[].url").type(JsonFieldType.STRING)
                                        .description("게시글 미디어 url"),
                                fieldWithPath("likeId").type(JsonFieldType.NUMBER)
                                        .description("좋아요 ID로 null -> 좋아요를 하지 않았을 경우, 요청한 유저가 해당 커뮤니티의 멤버가 아닌 경우"),
                                fieldWithPath("createdAt").type(JsonFieldType.STRING)
                                        .description("게시글 생성 일시"),
                                fieldWithPath("content").type(JsonFieldType.STRING)
                                        .description("내용"),
                                fieldWithPath("hashtags").type(JsonFieldType.ARRAY)
                                        .description("해시태그 목록").optional(),
                                fieldWithPath("likeCount").type(JsonFieldType.NUMBER)
                                        .description("좋아요 수"),
                                fieldWithPath("commentCount").type(JsonFieldType.NUMBER)
                                        .description("댓글 수"),
                                fieldWithPath("me").type(JsonFieldType.BOOLEAN)
                                        .description("true -> 내가 작성한 글인 경우")
                        )
                ));
    }

    @Test
    @DisplayName("글 수정")
    void testUpdatePost() throws Exception {
        final Long UPDATE_POST_ID = 2L;
        UpdatePostRequest request = new UpdatePostRequest("글 수정", List.of(), List.of());

        given(postCommandService.updatePost(any(UpdatePostRequest.class), anyLong(), anyLong()))
                .willReturn(UPDATE_POST_ID);

        ResultActions result = mvc.perform(
                patch("/api/posts/{postId}", UPDATE_POST_ID)
                        .contentType(APPLICATION_JSON)
                        .content(mapper.writeValueAsBytes(request))
                        .session(dummySession)
                        .header(HeaderConst.AUTH_TOKEN, TOKEN));

        result
                .andExpect(status().isOk())
                .andDo(document("post/patch-postId",
                        pathParameters(
                                parameterWithName("postId").description("게시글 ID")
                        ),
                        requestFields(
                                fieldWithPath("content").type(JsonFieldType.STRING)
                                        .description("글 내용"),
                                fieldWithPath("hashtags").type(JsonFieldType.ARRAY)
                                        .description("해시태그 목록"),
                                fieldWithPath("postMediaIds").type(JsonFieldType.ARRAY)
                                        .description("게시글 미디어 UUID 목록")
                        ),
                        responseFields(
                                fieldWithPath("id").type(JsonFieldType.NUMBER)
                                        .description("수정된 게시글 ID")
                        )
                ));
    }

    @Test
    @DisplayName("글 삭제")
    void testDeletePost() throws Exception {
        ResultActions result = mvc.perform(
                delete("/api/posts/{postId}", 1L)
                        .contentType(APPLICATION_JSON)
                        .session(dummySession)
                        .header(HeaderConst.AUTH_TOKEN, TOKEN));

        result
                .andExpect(status().isOk())
                .andDo(document("post/delete-postId",
                        pathParameters(
                                parameterWithName("postId").description("게시글 ID")
                        )
                ));
    }

    @Test
    @DisplayName("유저가 작성한 게시글을 페이지네이션해서 조회하기")
    void testGetUserPosts() throws Exception {
        UserPostDto.CommunityDto communityDto = new UserPostDto.CommunityDto(2L, "커뮤니티1");
        UserPostDto postsDto =
                new UserPostDto(1L, "게시글 내용1", communityDto, LocalDateTime.now(), null, null);

        UserPostPageResponse pageInfo =
                new UserPostPageResponse(List.of(postsDto), new PaginationDto(1, false));

        given(postQueryService.getUserPosts(anyLong(), anyLong(), any(Pageable.class)))
                .willReturn(pageInfo);

        ResultActions result = mvc.perform(
                get("/api/posts/users")
                        .queryParam("page", "0")
                        .queryParam("size", "1")
                        .contentType(APPLICATION_JSON)
                        .session(dummySession)
                        .header(HeaderConst.AUTH_TOKEN, TOKEN));

        result
                .andExpect(status().isOk())
                .andDo(document("post/get-users",
                        requestParameters(
                                parameterWithName("userId").description("조회할 유저 ID").optional(),
                                parameterWithName("page").description("페이지 번호"),
                                parameterWithName("size").description("페이지 사이즈")
                        ),
                        responseFields(
                                fieldWithPath("posts").type(JsonFieldType.ARRAY)
                                        .description("게시글 목록"),
                                fieldWithPath("posts[].id").type(JsonFieldType.NUMBER)
                                        .description("게시글 ID"),
                                fieldWithPath("posts[].content").type(JsonFieldType.STRING)
                                        .description("게시글 내용"),
                                fieldWithPath("posts[].community").type(JsonFieldType.OBJECT)
                                        .description("커뮤니티 정보"),
                                fieldWithPath("posts[].community.id").type(JsonFieldType.NUMBER)
                                        .description("커뮤니티 ID"),
                                fieldWithPath("posts[].community.name").type(JsonFieldType.STRING)
                                        .description("커뮤니티 이름"),
                                fieldWithPath("posts[].createdAt").type(JsonFieldType.STRING)
                                        .description("생성 일시"),
                                fieldWithPath("posts[].hashtags").type(JsonFieldType.ARRAY)
                                        .description("해시태그 목록").optional(),
                                fieldWithPath("posts[].postMedias").type(JsonFieldType.ARRAY)
                                        .description("게시글 미디어 목록").optional(),
                                fieldWithPath("posts[].postMedias.type").type(JsonFieldType.ARRAY)
                                        .description("게시글 미디어의 타입으로 IMG만 가능"),
                                fieldWithPath("posts[].postMedias.url").type(JsonFieldType.ARRAY)
                                        .description("게시글 미디어 url"),
                                fieldWithPath("pageInfo").type(JsonFieldType.OBJECT)
                                        .description("페이지네이션 정보"),
                                fieldWithPath("pageInfo.nextPage").type(JsonFieldType.NUMBER)
                                        .description("다음 페이지 번호"),
                                fieldWithPath("pageInfo.hasNext").type(JsonFieldType.BOOLEAN)
                                        .description("true -> 다음 페이지가 있는 경우")
                        )
                ));
    }

    @Test
    @DisplayName("글에 좋아요하기")
    void testDoLikeAtPost() throws Exception {
        final Long NEW_LIKE_ID = 2L;
        given(likeCommandService.doPostLike(anyLong(), anyLong()))
                .willReturn(NEW_LIKE_ID);

        ResultActions result = mvc.perform(
                post("/api/posts/{postId}/likes", 1L)
                        .contentType(APPLICATION_JSON)
                        .session(dummySession)
                        .header(HeaderConst.AUTH_TOKEN, TOKEN));

        result
                .andExpect(status().isOk())
                .andDo(document("post/post-postId-likes",
                        pathParameters(
                                parameterWithName("postId").description("게시글 ID")
                        ),
                        responseFields(
                                fieldWithPath("id").type(JsonFieldType.NUMBER).description("좋아요 ID")
                        )
                ));
    }

    @Test
    @DisplayName("글에 좋아요 한 유저들 조회하기")
    void testGetLikeMembersAtPost() throws Exception {
        UserBasicProfileDto userDto = new UserBasicProfileDto(1L, "url", "#0001", "유저");
        LikeMembersAtPostResponse response =
                new LikeMembersAtPostResponse(List.of(userDto), new PaginationDto(1, false));

        given(likeQueryService.getLikeMembersAtPost(anyLong(), anyLong(), any(Pageable.class)))
                .willReturn(response);

        ResultActions result = mvc.perform(
                get("/api/posts/{postId}/likes", 1L)
                        .queryParam("page", "0")
                        .queryParam("size", "1")
                        .contentType(APPLICATION_JSON)
                        .session(dummySession)
                        .header(HeaderConst.AUTH_TOKEN, TOKEN));

        result
                .andExpect(status().isOk())
                .andDo(document("post/get-postId-likes",
                        pathParameters(
                                parameterWithName("postId").description("게시글 ID")
                        ),
                        requestParameters(
                                parameterWithName("page").description("페이지 번호"),
                                parameterWithName("size").description("페이지 사이즈")
                        ),
                        responseFields(
                                fieldWithPath("members").type(JsonFieldType.ARRAY)
                                        .description("좋아요한 유저 목록"),
                                fieldWithPath("members[].id").type(JsonFieldType.NUMBER)
                                        .description("유저 ID"),
                                fieldWithPath("members[].name").type(JsonFieldType.STRING)
                                        .description("유저 이름"),
                                fieldWithPath("members[].tagNum").type(JsonFieldType.STRING)
                                        .description("태그 번호"),
                                fieldWithPath("members[].profileImageUrl").type(JsonFieldType.STRING)
                                        .description("프로필 이미지 주소"),
                                fieldWithPath("pageInfo").type(JsonFieldType.OBJECT)
                                        .description("페이지네이션 정보"),
                                fieldWithPath("pageInfo.nextPage").type(JsonFieldType.NUMBER)
                                        .description("다음 페이지 번호"),
                                fieldWithPath("pageInfo.hasNext").type(JsonFieldType.BOOLEAN)
                                        .description("true -> 다음 페이지가 있는 경우")
                        )
                ));
    }

    @Test
    @DisplayName("글에 달린 댓글들 조회하기")
    void testGetCommentsAtPost() throws Exception {
        UserBasicProfileDto userDto = new UserBasicProfileDto(1L, "url", "#1", "유저");
        CommentsAtPostResponse.MemberInfo memberInfo = new CommentsAtPostResponse.MemberInfo(2L, MemberType.MANAGER);

        CommentsAtPostResponse.ChildCommentInfo childCommentInfo =
                new CommentsAtPostResponse.ChildCommentInfo(4L, userDto, memberInfo, 5L, LocalDateTime.now(),
                        "자식댓글", 0L, false, 3L);

        CommentsAtPostResponse.ParentCommentInfo parentCommentInfo =
                new CommentsAtPostResponse.ParentCommentInfo(3L, userDto, memberInfo, 6L, LocalDateTime.now(),
                        "부모댓글", 0L, false, List.of(childCommentInfo));

        CommentsAtPostResponse response =
                new CommentsAtPostResponse(List.of(parentCommentInfo), new PaginationDto(1, false));

        given(commentQueryService.getCommentsAtPost(anyLong(), anyLong(), any(Pageable.class)))
                .willReturn(response);

        ResultActions result = mvc.perform(
                get("/api/posts/{postId}/comments", 6L)
                        .contentType(APPLICATION_JSON)
                        .queryParam("page", "0")
                        .queryParam("size", "1")
                        .session(dummySession)
                        .header(HeaderConst.AUTH_TOKEN, TOKEN));

        result
                .andExpect(status().isOk())
                .andDo(document("post/get-postId-comments",
                        pathParameters(
                                parameterWithName("postId").description("게시글 ID")
                        ),
                        requestParameters(
                                parameterWithName("page").description("페이지 번호"),
                                parameterWithName("size").description("페이지 사이즈")
                        ),
                        responseFields(
                                fieldWithPath("comments").type(JsonFieldType.ARRAY)
                                        .description("댓글 목록"),
                                fieldWithPath("comments[].id").type(JsonFieldType.NUMBER)
                                        .description("댓글 ID"),
                                fieldWithPath("comments[].user").type(JsonFieldType.OBJECT)
                                        .description("유저 정보"),
                                fieldWithPath("comments[].user.id").type(JsonFieldType.NUMBER)
                                        .description("유저 ID"),
                                fieldWithPath("comments[].user.profileImageUrl").type(JsonFieldType.STRING)
                                        .description("프로필 이미지 url").optional(),
                                fieldWithPath("comments[].user.tagNum").type(JsonFieldType.STRING)
                                        .description("태그번호"),
                                fieldWithPath("comments[].user.name").type(JsonFieldType.STRING)
                                        .description("유저 이름"),
                                fieldWithPath("comments[].member").type(JsonFieldType.OBJECT)
                                        .description("멤버 정보"),
                                fieldWithPath("comments[].member.id").type(JsonFieldType.NUMBER)
                                        .description("멤버 ID"),
                                fieldWithPath("comments[].member.memberType").type(JsonFieldType.STRING)
                                        .description("멤버 타입으로 MANAGER, SUB_MANAGER, NORMAL 중 하나"),
                                fieldWithPath("comments[].likeId").type(JsonFieldType.NUMBER)
                                        .description("좋아요 ID로 null -> 좋아요를 하지 않았을 경우, 요청한 유저가 해당 커뮤니티의 멤버가 아닌 경우"),
                                fieldWithPath("comments[].createdAt").type(JsonFieldType.STRING)
                                        .description("생성 일시"),
                                fieldWithPath("comments[].content").type(JsonFieldType.STRING)
                                        .description("댓글 내용"),
                                fieldWithPath("comments[].likeCount").type(JsonFieldType.NUMBER)
                                        .description("좋아요수"),
                                fieldWithPath("comments[].me").type(JsonFieldType.BOOLEAN)
                                        .description("true -> 내가 작성한 글인 경우"),
                                fieldWithPath("comments[].child").type(JsonFieldType.ARRAY)
                                        .description("자식 댓글 목록").optional(),

                                fieldWithPath("comments[].child[].id").type(JsonFieldType.NUMBER)
                                        .description("댓글 ID"),
                                fieldWithPath("comments[].child[].user").type(JsonFieldType.OBJECT)
                                        .description("유저 정보"),
                                fieldWithPath("comments[].child[].user.id").type(JsonFieldType.NUMBER)
                                        .description("유저 ID"),
                                fieldWithPath("comments[].child[].user.profileImageUrl").type(JsonFieldType.STRING)
                                        .description("프로필 이미지 url").optional(),
                                fieldWithPath("comments[].child[].user.tagNum").type(JsonFieldType.STRING)
                                        .description("태그번호"),
                                fieldWithPath("comments[].child[].user.name").type(JsonFieldType.STRING)
                                        .description("유저 이름"),
                                fieldWithPath("comments[].child[].member").type(JsonFieldType.OBJECT)
                                        .description("멤버 정보"),
                                fieldWithPath("comments[].child[].member.id").type(JsonFieldType.NUMBER)
                                        .description("멤버 ID"),
                                fieldWithPath("comments[].child[].member.memberType").type(JsonFieldType.STRING)
                                        .description("멤버 타입으로 MANAGER, SUB_MANAGER, NORMAL 중 하나"),
                                fieldWithPath("comments[].child[].likeId").type(JsonFieldType.NUMBER)
                                        .description("좋아요 ID로 null -> 좋아요를 하지 않았을 경우, 요청한 유저가 해당 커뮤니티의 멤버가 아닌 경우"),
                                fieldWithPath("comments[].child[].createdAt").type(JsonFieldType.STRING)
                                        .description("생성 일시"),
                                fieldWithPath("comments[].child[].content").type(JsonFieldType.STRING)
                                        .description("댓글 내용"),
                                fieldWithPath("comments[].child[].likeCount").type(JsonFieldType.NUMBER)
                                        .description("좋아요수"),
                                fieldWithPath("comments[].child[].me").type(JsonFieldType.BOOLEAN)
                                        .description("true -> 내가 작성한 글인 경우"),

                                fieldWithPath("pageInfo").type(JsonFieldType.OBJECT)
                                        .description("페이지네이션 정보"),
                                fieldWithPath("pageInfo.nextPage").type(JsonFieldType.NUMBER)
                                        .description("다음 페이지 번호"),
                                fieldWithPath("pageInfo.hasNext").type(JsonFieldType.BOOLEAN)
                                        .description("true -> 다음 페이지가 있는 경우")
                        )
                ));
    }

    @Test
    void 핫한게시물() throws Exception {
        HotPostDto hotPostDto =
                new HotPostDto(1L, 1, 1, "내용", 1L, List.of("hashtag"));

        HotPostsResponse response = new HotPostsResponse(List.of(hotPostDto));

        given(postQueryService.getHotPosts()).willReturn(response);

        ResultActions result = mvc.perform(
                get("/api/posts/hot")
                        .contentType(APPLICATION_JSON)
                        .session(dummySession)
                        .header(HeaderConst.AUTH_TOKEN, TOKEN));

        result
                .andExpect(status().isOk())
                .andDo(document("post/get-hot",
                        responseFields(
                                fieldWithPath("hots").type(JsonFieldType.ARRAY)
                                        .description("핫한 게시글 목록"),
                                fieldWithPath("hots[].postId").type(JsonFieldType.NUMBER)
                                        .description("게시글 ID"),
                                fieldWithPath("hots[].likeCount").type(JsonFieldType.NUMBER)
                                        .description("좋아요수"),
                                fieldWithPath("hots[].commentCount").type(JsonFieldType.NUMBER)
                                        .description("댓글수"),
                                fieldWithPath("hots[].content").type(JsonFieldType.STRING)
                                        .description("게시글 내용"),
                                fieldWithPath("hots[].communityId").type(JsonFieldType.NUMBER)
                                        .description("커뮤니티 ID"),
                                fieldWithPath("hots[].hashtags").type(JsonFieldType.ARRAY)
                                        .description("해시태그 목록").optional()
                        )
                ));
    }

    @Test
    void 게시물_검색() throws Exception {
        UserBasicProfileDto userDto = new UserBasicProfileDto(1L, null, "#0001", "김");
        List<PostMediaMetadataDto> postMediaDto = List.of(new PostMediaMetadataDto("123", "IMG"));

        SearchPostDto searchPostDto = new SearchPostDto(1L, userDto, 2L, "팍스",
                LocalDateTime.now(), List.of("해시태그"), postMediaDto, 1, 2, "내용");

        Slice<SearchPostDto> page = PageableUtil.getSlice(List.of(searchPostDto), PageRequest.of(0, 1));

        given(postQueryService.getSearchedPosts(any(), any(), anyLong()))
                .willReturn(page);

        ResultActions result = mvc.perform(
                get("/api/posts/search")
                        .contentType(APPLICATION_FORM_URLENCODED_VALUE)
                        .header(HeaderConst.AUTH_TOKEN, TOKEN)
                        .session(dummySession)
                        .queryParam("page", "0")
                        .queryParam("size", "1")
                        .queryParam("keyword", "헤헤")
                        .queryParam("order", PostListingOrder.LIKE_UPPER.toString()));

        result
                .andExpect(status().isOk())
                .andDo(document("post/get-search",
                        requestParameters(
                                parameterWithName("keyword").description("검색 키워드"),
                                parameterWithName("page").description("페이지 번호"),
                                parameterWithName("size").description("페이지 사이즈"),
                                parameterWithName("order")
                                        .description("기본값 NEWER(생성 최신순) / ORDER(생성 과거순) / LIKE_UPPER(좋아요 많은순)")
                        ),
                        responseFields(
                                fieldWithPath("posts").type(JsonFieldType.ARRAY)
                                        .description("게시글 목록"),
                                fieldWithPath("posts[].id").type(JsonFieldType.NUMBER)
                                        .description("게시글 ID"),
                                fieldWithPath("posts[].user").type(JsonFieldType.OBJECT)
                                        .description("유저 정보"),
                                fieldWithPath("posts[].user.id").type(JsonFieldType.NUMBER)
                                        .description("유저 ID"),
                                fieldWithPath("posts[].user.tagNum").type(JsonFieldType.STRING)
                                        .description("태그 번호"),
                                fieldWithPath("posts[].user.name").type(JsonFieldType.STRING)
                                        .description("유저 이름"),
                                fieldWithPath("posts[].communityId").type(JsonFieldType.NUMBER)
                                        .description("커뮤니티 ID"),
                                fieldWithPath("posts[].communityName").type(JsonFieldType.STRING)
                                        .description("커뮤니티 이름"),
                                fieldWithPath("posts[].likeCount").type(JsonFieldType.NUMBER)
                                        .description("좋아요수"),
                                fieldWithPath("posts[].commentCount").type(JsonFieldType.NUMBER)
                                        .description("댓글수"),
                                fieldWithPath("posts[].content").type(JsonFieldType.STRING)
                                        .description("게시글 내용"),
                                fieldWithPath("posts[].createdAt").type(JsonFieldType.STRING)
                                        .description("생성 일시"),
                                fieldWithPath("posts[].hashtags").type(JsonFieldType.ARRAY)
                                        .description("해시태그 목록").optional(),
                                fieldWithPath("posts[].postMedias").type(JsonFieldType.ARRAY)
                                        .description("게시글 미디어 목록").optional(),
                                fieldWithPath("posts[].postMedias[].url").type(JsonFieldType.STRING)
                                        .description("게시글 미디어 url"),
                                fieldWithPath("posts[].postMedias[].type").type(JsonFieldType.STRING)
                                        .description("게시글 미디어의 타입으로 IMG만 가능"),

                                fieldWithPath("pageInfo").type(JsonFieldType.OBJECT)
                                        .description("페이지네이션 정보"),
                                fieldWithPath("pageInfo.nextPage").type(JsonFieldType.NUMBER)
                                        .description("다음 페이지 번호"),
                                fieldWithPath("pageInfo.hasNext").type(JsonFieldType.BOOLEAN)
                                        .description("true -> 다음 페이지가 있는 경우")
                        )
                ));
    }
}