package boogi.apiserver.domain.community.community.application;

import boogi.apiserver.domain.community.community.dao.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.community.community.dto.dto.CommunitySettingInfoDto;
import boogi.apiserver.utils.TestEmptyEntityGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

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

        final Community community = TestEmptyEntityGenerator.Community();
        ReflectionTestUtils.setField(community, "id", 1L);

        given(communityRepository.findByCommunityId(anyLong()))
                .willReturn(community);

        //when
        Community communityWithHashTag = communityQueryService.getCommunityWithHashTag(anyLong());
        assertThat(communityWithHashTag).isEqualTo(community);
    }

    @Test
    @DisplayName("커뮤니티 설정정보 조회")
    void communitySettingInfo() {
        final Community community = TestEmptyEntityGenerator.Community();
        ReflectionTestUtils.setField(community, "autoApproval", true);
        ReflectionTestUtils.setField(community, "isPrivate", false);

        given(communityRepository.findByCommunityId(anyLong()))
                .willReturn(community);

        CommunitySettingInfoDto settingInfo = communityQueryService.getSettingInfo(anyLong());
        assertThat(settingInfo.getIsAuto()).isTrue();
        assertThat(settingInfo.getIsSecret()).isFalse();
    }
}