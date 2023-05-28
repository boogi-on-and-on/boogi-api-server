package boogi.apiserver.domain.community.community.application;

import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.community.community.dto.dto.CommunityDetailInfoDto;
import boogi.apiserver.domain.community.community.dto.dto.CommunityMetadataDto;
import boogi.apiserver.domain.community.community.dto.dto.CommunitySettingInfoDto;
import boogi.apiserver.domain.community.community.dto.dto.JoinedCommunitiesDto;
import boogi.apiserver.domain.community.community.dto.response.CommunityDetailResponse;
import boogi.apiserver.domain.community.community.repository.CommunityRepository;
import boogi.apiserver.domain.member.application.MemberQuery;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.member.repository.MemberRepository;
import boogi.apiserver.domain.notice.application.NoticeQuery;
import boogi.apiserver.domain.notice.dto.dto.NoticeDto;
import boogi.apiserver.domain.post.post.application.PostQuery;
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
import org.junit.jupiter.api.BeforeEach;
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
class CommunityQueryTest {

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
    MemberQuery memberQuery;
    @Mock
    NoticeQuery noticeQuery;
    @Mock
    PostQuery postQuery;

    @InjectMocks
    CommunityQuery communityQuery;

    private final String TAG = "태그1";
    private Community community = POCS.toCommunity(1L, List.of(TAG));
    private User user = UserFixture.DEOKHWAN.toUser(2L);
    private Member member = MemberFixture.DEOKHWAN_POCS.toMember(3L, user, community);
    private Post post1 = POST1.toPost(4L, member, community, List.of(TAG), null);
    private PostMedia postMedia;

    @BeforeEach
    public void init() {
        this.postMedia = PostMedia.builder()
                .mediaURL("url")
                .post(post1)
                .build();
        post1.addPostMedias(List.of(postMedia));
    }

    @Test
    @DisplayName("커뮤니티 상세 조회")
    void communityDetail() {
        //given
        given(communityRepository.findCommunityById(anyLong()))
                .willReturn(community);

        given(memberQuery.getMemberOrNullMember(anyLong(), any(Community.class)))
                .willReturn(member);

        NoticeDto noticeDto = NoticeDto.from(NoticeFixture.NOTICE1.toNotice(1L, community, member));
        given(noticeQuery.getCommunityLatestNotice(anyLong()))
                .willReturn(List.of(noticeDto));

        LatestCommunityPostDto postDto = LatestCommunityPostDto.of(post1);
        given(postQuery.getLatestPostOfCommunity(any(Member.class), any(Community.class)))
                .willReturn(List.of(postDto));

        //when
        final CommunityDetailResponse response = communityQuery.getCommunityDetail(user.getId(), community.getId());

        //then
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
                .containsExactly(tuple(post1.getId(), POST1.content, POST1.createdAt));
    }

    @Test
    @DisplayName("커뮤니티 메타정보 조회")
    void communityMetadata() {
        //given
        given(communityRepository.findCommunityById(anyLong()))
                .willReturn(community);

        //when
        final CommunityMetadataDto dto = communityQuery.getCommunityMetadata(user.getId(), community.getId());

        //then
        then(memberQuery).should(times(1)).getManager(anyLong(), anyLong());

        assertThat(dto.getName()).isEqualTo(POCS.communityName);
        assertThat(dto.getIntroduce()).isEqualTo(POCS.description);
        assertThat(dto.getHashtags()).containsExactly("태그1");
    }

    @Test
    @DisplayName("커뮤니티 설정 조회")
    void communitySettingInfo() {
        //given
        given(communityRepository.findCommunityById(anyLong()))
                .willReturn(community);

        //when
        CommunitySettingInfoDto dto = communityQuery.getSetting(user.getId(), community.getId());

        //then
        then(memberQuery).should(times(1)).getManager(anyLong(), anyLong());
        assertThat(dto.getIsAuto()).isEqualTo(POCS.autoApproval);
        assertThat(dto.getIsSecret()).isEqualTo(POCS.isPrivate);
    }

    @Test
    @DisplayName("내가 가입한 커뮤니티의 최신글 조회")
    void joinedCommunitiesWithLatestPost() {
        //given
        given(memberRepository.findMembersWithCommunity(anyLong()))
                .willReturn(List.of(member));

        given(postRepository.getLatestPostByCommunityIds(anySet()))
                .willReturn(List.of(post1));

        given(postMediaRepository.getPostMediasByLatestPostIds(any()))
                .willReturn(List.of(postMedia));

        //when
        final JoinedCommunitiesDto dto = communityQuery.getJoinedCommunitiesWithLatestPost(user.getId());

        //then
        then(userRepository).should(times(1)).findUserById(anyLong());

        final JoinedCommunitiesDto.CommunityInfo communityInfo = dto.getCommunities().get(0);
        assertThat(communityInfo.getId()).isEqualTo(community.getId());
        assertThat(communityInfo.getName()).isEqualTo(POCS.communityName);

        final JoinedCommunitiesDto.PostInfo postInfo = communityInfo.getPost();
        assertThat(postInfo.getId()).isEqualTo(post1.getId());
        assertThat(postInfo.getHashtags()).containsExactly(TAG);
        assertThat(postInfo.getContent()).isEqualTo(POST1.content);
        assertThat(postInfo.getPostMediaUrl()).isEqualTo(postMedia.getMediaURL());
        assertThat(postInfo.getLikeCount()).isEqualTo(POST1.likeCount);
        assertThat(postInfo.getCommentCount()).isEqualTo(POST1.commentCount);
    }
}