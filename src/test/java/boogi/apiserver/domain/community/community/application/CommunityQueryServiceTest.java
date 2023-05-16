package boogi.apiserver.domain.community.community.application;

import boogi.apiserver.builder.TestCommunity;
import boogi.apiserver.builder.TestMember;
import boogi.apiserver.builder.TestPost;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.community.community.dto.dto.CommunityDetailInfoDto;
import boogi.apiserver.domain.community.community.dto.dto.CommunityMetadataDto;
import boogi.apiserver.domain.community.community.dto.dto.CommunitySettingInfoDto;
import boogi.apiserver.domain.community.community.dto.dto.JoinedCommunitiesDto;
import boogi.apiserver.domain.community.community.dto.response.CommunityDetailResponse;
import boogi.apiserver.domain.community.community.repository.CommunityRepository;
import boogi.apiserver.domain.member.application.MemberQueryService;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.member.repository.MemberRepository;
import boogi.apiserver.domain.notice.application.NoticeQueryService;
import boogi.apiserver.domain.notice.dto.dto.NoticeDto;
import boogi.apiserver.domain.post.post.application.PostQueryService;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.post.post.dto.dto.LatestCommunityPostDto;
import boogi.apiserver.domain.post.post.repository.PostRepository;
import boogi.apiserver.domain.post.postmedia.domain.PostMedia;
import boogi.apiserver.domain.post.postmedia.repository.PostMediaRepository;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.domain.user.repository.UserRepository;
import boogi.apiserver.utils.fixture.MemberFixture;
import boogi.apiserver.utils.fixture.NoticeFixture;
import boogi.apiserver.utils.fixture.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static boogi.apiserver.utils.fixture.CommunityFixture.POCS;
import static boogi.apiserver.utils.fixture.PostFixture.POST1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class CommunityQueryServiceTest {

    @Mock
    CommunityRepository communityRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    MemberRepository memberRepository;
    @Mock
    PostRepository postRepository;
    @Mock
    PostMediaRepository postMediaRepository;

    @Mock
    MemberQueryService memberQueryService;
    @Mock
    NoticeQueryService noticeQueryService;
    @Mock
    PostQueryService postQueryService;

    @InjectMocks
    CommunityQueryService communityQueryService;


    @Test
    @DisplayName("커뮤니티 상세 조회")
    void communityDetail() {
        final String TAG = "태그1";

        Community community = POCS.toCommunity(List.of(TAG));
        given(communityRepository.findCommunityById(anyLong()))
                .willReturn(community);

        User user = UserFixture.DEOKHWAN.toUser();
        Member member = MemberFixture.DEOKHWAN_POCS.toMember(user, community);
        given(memberQueryService.getMemberOrNullMember(anyLong(), any(Community.class)))
                .willReturn(member);

        NoticeDto noticeDto = NoticeDto.from(NoticeFixture.NOTICE1.toNotice(1L, community, member));
        given(noticeQueryService.getCommunityLatestNotice(anyLong()))
                .willReturn(List.of(noticeDto));

        Post post1 = POST1.toPost(1L, member, community, null, null);
        LatestCommunityPostDto postDto = LatestCommunityPostDto.of(post1);

        given(postQueryService.getLatestPostOfCommunity(any(Member.class), any(Community.class)))
                .willReturn(List.of(postDto));

        final CommunityDetailResponse response = communityQueryService.getCommunityDetail(1L, 1L);

        assertThat(response.getSessionMemberType()).isEqualTo(MemberType.NORMAL);

        final CommunityDetailInfoDto communityDto = response.getCommunity();
        assertThat(communityDto.getIsPrivated()).isFalse();
        assertThat(communityDto.getCategory()).isEqualTo(POCS.communityCategory);
        assertThat(communityDto.getName()).isEqualTo(POCS.communityName);
        assertThat(communityDto.getIntroduce()).isEqualTo(POCS.description);
        assertThat(communityDto.getHashtags()).containsExactly(TAG);
        assertThat(communityDto.getMemberCount()).isEqualTo(POCS.memberCount);
        assertThat(communityDto.getCreatedAt()).isEqualTo(POCS.createdAt);

        assertThat(response.getPosts()).hasSize(1)
                .extracting("id", "content", "createdAt")
                .containsExactly(tuple(1L, POST1.content, POST1.createdAt));
    }

    @Test
    @DisplayName("커뮤니티 메타정보 조회")
    void communityMetadata() {
        final Community community = TestCommunity.builder()
                .communityName("커뮤니티 이름")
                .description("커뮤니티 소개란입니다.")
                .build();
        community.addTags(List.of("태그1"));

        given(communityRepository.findCommunityById(anyLong()))
                .willReturn(community);

        final CommunityMetadataDto dto = communityQueryService.getCommunityMetadata(1L, 1L);

        then(memberQueryService).should(times(1)).getManager(anyLong(), anyLong());

        assertThat(dto.getName()).isEqualTo("커뮤니티 이름");
        assertThat(dto.getIntroduce()).isEqualTo("커뮤니티 소개란입니다.");
        assertThat(dto.getHashtags()).containsExactly("태그1");
    }

    @Test
    @DisplayName("커뮤니티 설정 조회")
    void communitySettingInfo() {
        final Community community = TestCommunity.builder()
                .autoApproval(true)
                .isPrivate(false)
                .build();

        given(communityRepository.findCommunityById(anyLong()))
                .willReturn(community);

        CommunitySettingInfoDto dto = communityQueryService.getSetting(1L, 1L);

        then(memberQueryService).should(times(1)).getManager(anyLong(), anyLong());
        assertThat(dto.getIsAuto()).isTrue();
        assertThat(dto.getIsSecret()).isFalse();
    }

    @Test
    @DisplayName("내가 가입한 커뮤니티의 최신글 조회")
    void joinedCommunitiesWithLatestPost() {
        final Community community = TestCommunity.builder()
                .id(1L)
                .communityName("커뮤니티 이름")
                .build();
        final Member member = TestMember.builder()
                .community(community)
                .build();

        given(memberRepository.findMembersWithCommunity(anyLong()))
                .willReturn(List.of(member));

        final Post post = TestPost.builder()
                .id(1L)
                .community(community)
                .content("게시글 내용입니다.")
                .likeCount(1)
                .commentCount(1)
                .build();
        post.addTags(List.of("태그1"));

        given(postRepository.getLatestPostByCommunityIds(anySet()))
                .willReturn(List.of(post));

        final PostMedia postMedia = PostMedia.builder()
                .mediaURL("url")
                .post(post)
                .build();
        given(postMediaRepository.getPostMediasByLatestPostIds(any()))
                .willReturn(List.of(postMedia));

        final JoinedCommunitiesDto dto = communityQueryService.getJoinedCommunitiesWithLatestPost(1L);

        then(userRepository).should(times(1)).findUserById(anyLong());

        final JoinedCommunitiesDto.CommunityInfo communityInfo = dto.getCommunities().get(0);
        assertThat(communityInfo.getId()).isEqualTo(1L);
        assertThat(communityInfo.getName()).isEqualTo("커뮤니티 이름");

        final JoinedCommunitiesDto.PostInfo postInfo = communityInfo.getPost();
        assertThat(postInfo.getId()).isEqualTo(1L);
        assertThat(postInfo.getHashtags()).containsExactly("태그1");
        assertThat(postInfo.getContent()).isEqualTo("게시글 내용입니다.");
        assertThat(postInfo.getPostMediaUrl()).isEqualTo("url");
        assertThat(postInfo.getLikeCount()).isEqualTo(1);
        assertThat(postInfo.getCommentCount()).isEqualTo(1);
    }
}