package boogi.apiserver.domain.community.community.application;

import boogi.apiserver.builder.TestCommunity;
import boogi.apiserver.domain.community.community.dao.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.community.community.dto.dto.CommunitySettingInfoDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CommunityQueryServiceTest {

    @Mock
    CommunityRepository communityRepository;

    @InjectMocks
    CommunityQueryService communityQueryService;

    @Test
    @DisplayName("커뮤니티 기본 정보 조회")
    void communityBasicInfo() {
        //given
        final Community community = TestCommunity.builder()
                .id(1L)
                .build();

        given(communityRepository.findByCommunityId(anyLong()))
                .willReturn(community);

        //when
        Community communityWithHashTag = communityQueryService.getCommunityWithHashTag(anyLong());
        assertThat(communityWithHashTag).isEqualTo(community);
    }

    @Test
    @DisplayName("커뮤니티 설정정보 조회")
    void communitySettingInfo() {
        final Community community = TestCommunity.builder()
                .autoApproval(true)
                .isPrivate(false)
                .build();

        given(communityRepository.findByCommunityId(anyLong()))
                .willReturn(community);

//        CommunitySettingInfoDto settingInfo = communityQueryService.getSetting(anyLong());
//        assertThat(settingInfo.getIsAuto()).isTrue();
//        assertThat(settingInfo.getIsSecret()).isFalse();
    }
}