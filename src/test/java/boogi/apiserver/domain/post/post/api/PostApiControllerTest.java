package boogi.apiserver.domain.post.post.api;

import boogi.apiserver.domain.comment.application.CommentQueryService;
import boogi.apiserver.domain.comment.dto.response.CommentsAtPostResponse;
import boogi.apiserver.domain.community.community.exception.CommunityNotFoundException;
import boogi.apiserver.domain.like.application.LikeCommandService;
import boogi.apiserver.domain.like.application.LikeQueryService;
import boogi.apiserver.domain.like.dto.response.LikeMembersAtPostResponse;
import boogi.apiserver.domain.like.exception.AlreadyDoPostLikeException;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.member.exception.CanNotDeletePostException;
import boogi.apiserver.domain.member.exception.CanNotUpdatePostException;
import boogi.apiserver.domain.member.exception.NotJoinedMemberException;
import boogi.apiserver.domain.member.exception.NotViewableMemberException;
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
import boogi.apiserver.domain.post.post.exception.PostNotFoundException;
import boogi.apiserver.domain.post.postmedia.domain.MediaType;
import boogi.apiserver.domain.post.postmedia.dto.dto.PostMediaMetadataDto;
import boogi.apiserver.domain.post.postmedia.exception.UnmappedPostMediaExcecption;
import boogi.apiserver.domain.user.dto.dto.UserBasicProfileDto;
import boogi.apiserver.global.constant.HeaderConst;
import boogi.apiserver.global.dto.PaginationDto;
import boogi.apiserver.global.util.PageableUtil;
import boogi.apiserver.global.webclient.push.SendPushNotification;
import boogi.apiserver.utils.controller.TestControllerSetUp;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import static org.mockito.Mockito.doThrow;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.restdocs.snippet.Attributes.key;
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

    @Nested
    @DisplayName("게시글 생성")
    class CreatePost {

        @Test
        @DisplayName("게시글 생성에 성공한다.")
        void createPostSuccess() throws Exception {
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
                    .andDo(document("posts/post",
                            requestFields(
                                    fieldWithPath("communityId").type(JsonFieldType.NUMBER)
                                            .description("커뮤니티 ID"),
                                    fieldWithPath("content").type(JsonFieldType.STRING)
                                            .description("게시글 내용")
                                            .attributes(key("constraint").value("10 ~ 1000 길이의 문자열")),
                                    fieldWithPath("hashtags").type(JsonFieldType.ARRAY)
                                            .description("해시태그 목록")
                                            .attributes(key("constraint").value("1 ~ 10의 길이를 가지는 한글,영어,숫자로만 구성된 문자열로 최대 5개까지 입력 가능")),
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
        @DisplayName("존재하지 않은 커뮤니티 ID로 요청시 CommunityNotFoundException 발생")
        void notExistCommunityFail() throws Exception {
            CreatePostRequest request =
                    new CreatePostRequest(9999L, "글", List.of(), List.of(), List.of());

            doThrow(new CommunityNotFoundException())
                    .when(postCommandService).createPost(any(CreatePostRequest.class), anyLong());

            ResultActions result = mvc.perform(
                    post("/api/posts/")
                            .contentType(APPLICATION_JSON)
                            .content(mapper.writeValueAsBytes(request))
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN));

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("posts/post-CommunityNotFoundException"));
        }

        @Test
        @DisplayName("해당 커뮤니티에 가입되지 않는 경우 NotJoinedMemberException 발생")
        void notMemberFail() throws Exception {
            CreatePostRequest request =
                    new CreatePostRequest(1L, "글", List.of(), List.of(), List.of());

            doThrow(new NotJoinedMemberException())
                    .when(postCommandService).createPost(any(CreatePostRequest.class), anyLong());

            ResultActions result = mvc.perform(
                    post("/api/posts/")
                            .contentType(APPLICATION_JSON)
                            .content(mapper.writeValueAsBytes(request))
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN));

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("posts/post-NotJoinedMemberException"));
        }

        @Test
        @DisplayName("요청한 게시글 미디어 UUID들과 매핑되지않은 게시글 미디어의 숫자와 맞지 않을 경우 UnmappedPostMediaExcecption 발생")
        void unmappedPostMediaFail() throws Exception {
            CreatePostRequest request =
                    new CreatePostRequest(1L, "글", List.of(), List.of("uuid"), List.of());

            doThrow(new UnmappedPostMediaExcecption())
                    .when(postCommandService).createPost(any(CreatePostRequest.class), anyLong());

            ResultActions result = mvc.perform(
                    post("/api/posts/")
                            .contentType(APPLICATION_JSON)
                            .content(mapper.writeValueAsBytes(request))
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN));

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("posts/post-UnmappedPostMediaExcecption"));
        }
    }

    @Nested
    @DisplayName("게시글 수정")
    class UpdatePost {
        @Test
        @DisplayName("게시글 수정에 성공한다.")
        void updatePostSuccess() throws Exception {
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
                    .andDo(document("posts/patch-postId",
                            pathParameters(
                                    parameterWithName("postId").description("게시글 ID")
                            ),
                            requestFields(
                                    fieldWithPath("content").type(JsonFieldType.STRING)
                                            .description("글 내용")
                                            .attributes(key("constraint").value("10 ~ 1000 길이의 문자열")),
                                    fieldWithPath("hashtags").type(JsonFieldType.ARRAY)
                                            .description("해시태그 목록")
                                            .attributes(key("constraint").value("1 ~ 10의 길이를 가지는 한글,영어,숫자로만 구성된 문자열로 최대 5개까지 입력 가능")),
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
        @DisplayName("존재하지 않은 게시글 ID로 요청시 PostNotFoundException 발생")
        void notExistPostFail() throws Exception {
            UpdatePostRequest request = new UpdatePostRequest("글 수정", List.of(), List.of());

            doThrow(new PostNotFoundException())
                    .when(postCommandService).updatePost(any(UpdatePostRequest.class), anyLong(), anyLong());

            ResultActions result = mvc.perform(
                    patch("/api/posts/{postId}", 9999L)
                            .contentType(APPLICATION_JSON)
                            .content(mapper.writeValueAsBytes(request))
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN));

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("posts/patch-postId-PostNotFoundException"));
        }

        @Test
        @DisplayName("게시자가 아닌 경우 CanNotUpdatePostException 발생")
        void notAuthorFail() throws Exception {
            UpdatePostRequest request = new UpdatePostRequest("글 수정", List.of(), List.of());

            doThrow(new CanNotUpdatePostException())
                    .when(postCommandService).updatePost(any(UpdatePostRequest.class), anyLong(), anyLong());

            ResultActions result = mvc.perform(
                    patch("/api/posts/{postId}", 1L)
                            .contentType(APPLICATION_JSON)
                            .content(mapper.writeValueAsBytes(request))
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN));

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("posts/patch-postId-CanNotUpdatePostException"));
        }
    }

    @Nested
    @DisplayName("게시글 상세 조회")
    class GetPostDetail {
        @Test
        @DisplayName("게시글 상세 조회에 성공한다.")
        void getPostDetailSuccess() throws Exception {
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
                    .andDo(document("posts/get-postId",
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
        @DisplayName("존재하지 않은 게시글 ID로 요청시 PostNotFoundException 발생")
        void notExistPostFail() throws Exception {
            doThrow(new PostNotFoundException())
                    .when(postQueryService).getPostDetail(anyLong(), anyLong());

            ResultActions result = mvc.perform(
                    get("/api/posts/{postId}", 9999L)
                            .contentType(APPLICATION_JSON)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN));

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("posts/get-postId-PostNotFoundException"));
        }

        @Test
        @DisplayName("비공개 커뮤니티의 게시글을 비가입 유저로 요청시 NotViewableMemberException 발생")
        void notViewableMemberFail() throws Exception {
            doThrow(new NotViewableMemberException())
                    .when(postQueryService).getPostDetail(anyLong(), anyLong());

            ResultActions result = mvc.perform(
                    get("/api/posts/{postId}", 1L)
                            .contentType(APPLICATION_JSON)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN));

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("posts/get-postId-NotViewableMemberException"));
        }
    }

    @Nested
    @DisplayName("게시글 삭제")
    class DeletePost {
        @Test
        @DisplayName("게시글 삭제에 성공한다.")
        void deletePostSuccess() throws Exception {
            ResultActions result = mvc.perform(
                    delete("/api/posts/{postId}", 1L)
                            .contentType(APPLICATION_JSON)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN));

            result
                    .andExpect(status().isOk())
                    .andDo(document("posts/delete-postId",
                            pathParameters(
                                    parameterWithName("postId").description("게시글 ID")
                            )
                    ));
        }

        @Test
        @DisplayName("존재하지 않은 게시글 ID로 요청시 PostNotFoundException 발생")
        void notExistPostFail() throws Exception {
            doThrow(new PostNotFoundException())
                    .when(postCommandService).deletePost(anyLong(), anyLong());

            ResultActions result = mvc.perform(
                    delete("/api/posts/{postId}", 9999L)
                            .contentType(APPLICATION_JSON)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN));

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("posts/delete-postId-PostNotFoundException"));
        }

        @Test
        @DisplayName("해당 게시글의 게시자와 해당 커뮤니티의 관리자가 아닌 경우 CanNotDeletePostException 발생")
        void notAuthorAndOperatorFail() throws Exception {
            doThrow(new CanNotDeletePostException())
                    .when(postCommandService).deletePost(anyLong(), anyLong());

            ResultActions result = mvc.perform(
                    delete("/api/posts/{postId}", 9999L)
                            .contentType(APPLICATION_JSON)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN));

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("posts/delete-postId-CanNotDeletePostException"));
        }
    }

    @Test
    @DisplayName("유저가 작성한 게시글을 페이지네이션해서 조회한다.")
    void getUserPostsSuccess() throws Exception {
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
                .andDo(document("posts/get-users",
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
    @DisplayName("핫한 게시물 조회에 성공한다.")
    void getHotPostsSuccess() throws Exception {
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
                .andDo(document("posts/get-hot",
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

    @Nested
    @DisplayName("게시글에 좋아요하기")
    class DoLikeAtPost {
        @Test
        @DisplayName("게시글에 좋아요 하기에 성공한다.")
        void doLikeAtPostSuccess() throws Exception {
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
                    .andDo(document("posts/post-postId-likes",
                            pathParameters(
                                    parameterWithName("postId").description("게시글 ID")
                            ),
                            responseFields(
                                    fieldWithPath("id").type(JsonFieldType.NUMBER).description("좋아요 ID")
                            )
                    ));
        }

        @Test
        @DisplayName("존재하지 않은 게시글 ID로 요청시 PostNotFoundException 발생")
        void notExistPostFail() throws Exception {
            doThrow(new PostNotFoundException())
                    .when(likeCommandService).doPostLike(anyLong(), anyLong());

            ResultActions result = mvc.perform(
                    post("/api/posts/{postId}/likes", 9999L)
                            .contentType(APPLICATION_JSON)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN));

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("posts/post-postId-likes-PostNotFoundException"));
        }

        @Test
        @DisplayName("해당 게시글이 작성된 커뮤니티에 가입되지 않는 경우 NotJoinedMemberException 발생")
        void notMemberFail() throws Exception {
            doThrow(new NotJoinedMemberException())
                    .when(likeCommandService).doPostLike(anyLong(), anyLong());

            ResultActions result = mvc.perform(
                    post("/api/posts/{postId}/likes", 1L)
                            .contentType(APPLICATION_JSON)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN));

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("posts/post-postId-likes-NotJoinedMemberException"));
        }

        @Test
        @DisplayName("이미 해당 게시글에 좋아요를 한 경우 AlreadyDoPostLikeException 발생")
        void alreadyDoPostLikeFail() throws Exception {
            doThrow(new AlreadyDoPostLikeException())
                    .when(likeCommandService).doPostLike(anyLong(), anyLong());

            ResultActions result = mvc.perform(
                    post("/api/posts/{postId}/likes", 1L)
                            .contentType(APPLICATION_JSON)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN));

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("posts/post-postId-likes-AlreadyDoPostLikeException"));
        }
    }

    @Nested
    @DisplayName("글에 좋아요 한 유저들 목록 조회")
    class GetLikeMembersAtPost {
        @Test
        @DisplayName("글에 좋아요 한 유저들 목록 조회에 성공한다.")
        void getLikeMembersAtPostSuccess() throws Exception {
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
                    .andDo(document("posts/get-postId-likes",
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
        @DisplayName("존재하지 않은 게시글 ID로 요청시 PostNotFoundException 발생")
        void notExistPostFail() throws Exception {
            doThrow(new PostNotFoundException())
                    .when(likeQueryService).getLikeMembersAtPost(anyLong(), anyLong(), any(Pageable.class));

            ResultActions result = mvc.perform(
                    get("/api/posts/{postId}/likes", 9999L)
                            .queryParam("page", "0")
                            .queryParam("size", "1")
                            .contentType(APPLICATION_JSON)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN));

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("posts/get-postId-likes-PostNotFoundException"));
        }

        @Test
        @DisplayName("비공개 커뮤니티의 게시글 좋아요 유저 목록을 비가입 유저로 요청시 NotViewableMemberException 발생")
        void notViewableMemberFail() throws Exception {
            doThrow(new NotViewableMemberException())
                    .when(likeQueryService).getLikeMembersAtPost(anyLong(), anyLong(), any(Pageable.class));

            ResultActions result = mvc.perform(
                    get("/api/posts/{postId}/likes", 1L)
                            .queryParam("page", "0")
                            .queryParam("size", "1")
                            .contentType(APPLICATION_JSON)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN));

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("posts/get-postId-likes-NotViewableMemberException"));
        }
    }

    @Nested
    @DisplayName("게시글에 달린 댓글들 목록 조회")
    class GetCommentsAtPost {
        @Test
        @DisplayName("게시글에 달린 댓글들 목록 조회에 성공한다.")
        void getCommentsAtPostSuccess() throws Exception {
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
                    .andDo(document("posts/get-postId-comments",
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
        @DisplayName("존재하지 않은 게시글 ID로 요청시 PostNotFoundException 발생")
        void notExistPostFail() throws Exception {
            doThrow(new PostNotFoundException())
                    .when(commentQueryService).getCommentsAtPost(anyLong(), anyLong(), any(Pageable.class));

            ResultActions result = mvc.perform(
                    get("/api/posts/{postId}/comments", 9999L)
                            .contentType(APPLICATION_JSON)
                            .queryParam("page", "0")
                            .queryParam("size", "1")
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN));

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("posts/get-postId-comments-PostNotFoundException"));
        }

        @Test
        @DisplayName("비공개 커뮤니티의 게시글에 달린 댓글들을 비가입 유저로 요청시 NotViewableMemberException 발생")
        void notViewableMemberFail() throws Exception {
            doThrow(new NotViewableMemberException())
                    .when(commentQueryService).getCommentsAtPost(anyLong(), anyLong(), any(Pageable.class));

            ResultActions result = mvc.perform(
                    get("/api/posts/{postId}/comments", 1L)
                            .contentType(APPLICATION_JSON)
                            .queryParam("page", "0")
                            .queryParam("size", "1")
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN));

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("posts/get-postId-comments-NotViewableMemberException"));
        }
    }

    @Test
    @DisplayName("검색한 게시글을 페이지네이션해서 조회한다.")
    void getSearchPostsSuccess() throws Exception {
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
                .andDo(document("posts/get-search",
                        requestParameters(
                                parameterWithName("keyword").description("검색 키워드"),
                                parameterWithName("page").description("페이지 번호"),
                                parameterWithName("size").description("페이지 사이즈"),
                                parameterWithName("order").description("정렬 순서")
                                        .attributes(key("constraint").value("기본값 NEWER(생성 최신순) / ORDER(생성 과거순) / LIKE_UPPER(좋아요 많은순) 중 하나"))
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