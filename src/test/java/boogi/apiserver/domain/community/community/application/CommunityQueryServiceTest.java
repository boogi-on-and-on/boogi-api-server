package boogi.apiserver.domain.community.community.application;

import boogi.apiserver.builder.TestCommunity;
import boogi.apiserver.builder.TestMember;
import boogi.apiserver.builder.TestPost;
import boogi.apiserver.domain.community.community.dao.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.community.community.domain.CommunityCategory;
import boogi.apiserver.domain.community.community.dto.dto.CommunityDetailInfoDto;
import boogi.apiserver.domain.community.community.dto.dto.CommunityMetadataDto;
import boogi.apiserver.domain.community.community.dto.dto.CommunitySettingInfoDto;
import boogi.apiserver.domain.community.community.dto.dto.JoinedCommunitiesDto;
import boogi.apiserver.domain.community.community.dto.response.CommunityDetailResponse;
import boogi.apiserver.domain.member.application.MemberQueryService;
import boogi.apiserver.domain.member.dao.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.notice.application.NoticeQueryService;
import boogi.apiserver.domain.notice.dto.dto.NoticeDto;
import boogi.apiserver.domain.post.post.application.PostQueryService;
import boogi.apiserver.domain.post.post.dao.PostRepository;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.post.post.dto.dto.LatestCommunityPostDto;
import boogi.apiserver.domain.post.postmedia.dao.PostMediaRepository;
import boogi.apiserver.domain.post.postmedia.domain.PostMedia;
import boogi.apiserver.domain.user.dao.UserRepository;
import boogi.apiserver.utils.TestTimeReflection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
        final Community community = TestCommunity.builder()
                .isPrivate(true)
                .category(CommunityCategory.ACADEMIC)
                .communityName("커뮤니티")
                .description("커뮤니티 소개란입니다.")
                .memberCount(1)
                .build();
        community.addTags(List.of("태그1"));
        TestTimeReflection.setCreatedAt(community, LocalDateTime.now());
        given(communityRepository.findCommunityById(anyLong()))
                .willReturn(community);

        final Member member = TestMember.builder()
                .memberType(MemberType.NORMAL)
                .build();
        given(memberQueryService.getMemberOrNullMember(anyLong(), any(Community.class)))
                .willReturn(member);

        final NoticeDto noticeDto = new NoticeDto(1L, "공지1", LocalDateTime.now());
        given(noticeQueryService.getCommunityLatestNotice(anyLong()))
                .willReturn(List.of(noticeDto));

        final LatestCommunityPostDto postDto = LatestCommunityPostDto.builder()
                .id(1L)
                .content("글1")
                .createdAt(LocalDateTime.now())
                .build();
        given(postQueryService.getLatestPostOfCommunity(any(Member.class), any(Community.class)))
                .willReturn(List.of(postDto));

        final CommunityDetailResponse response = communityQueryService.getCommunityDetail(1L, 1L);

        assertThat(response.getSessionMemberType()).isEqualTo(MemberType.NORMAL);

        final CommunityDetailInfoDto detail = response.getCommunity();
        assertThat(detail.getIsPrivated()).isEqualTo(true);
        assertThat(detail.getCategory().toString()).isEqualTo("ACADEMIC");
        assertThat(detail.getName()).isEqualTo("커뮤니티");
        assertThat(detail.getIntroduce()).isEqualTo("커뮤니티 소개란입니다.");
        assertThat(detail.getHashtags()).containsExactly("태그1");
        assertThat(detail.getMemberCount()).isEqualTo("1");
        assertThat(detail.getCreatedAt()).isEqualTo(community.getCreatedAt().toString());

        final LatestCommunityPostDto post = response.getPosts().get(0);
        assertThat(post.getId()).isEqualTo(1L);
        assertThat(post.getContent()).isEqualTo("글1");
        assertThat(post.getCreatedAt()).isEqualTo(postDto.getCreatedAt());
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

        then(userRepository).should(times(1)).findByUserId(anyLong());

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