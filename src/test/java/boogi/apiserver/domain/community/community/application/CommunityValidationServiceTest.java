package boogi.apiserver.domain.community.community.application;

import boogi.apiserver.domain.community.community.dao.CommunityRepository;
import boogi.apiserver.domain.community.community.exception.AlreadyExistsCommunityNameException;
import boogi.apiserver.global.error.exception.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;


@ExtendWith(MockitoExtension.class)
class CommunityValidationServiceTest {

    @Mock
    CommunityRepository communityRepository;

    @InjectMocks
    CommunityValidationService communityValidationService;


    @Test
    void 커뮤니티_이미_동일한_이름() {
        //given
        given(communityRepository.findByCommunityNameEquals(anyString()))
                .willThrow(new AlreadyExistsCommunityNameException());

        //then
        assertThatThrownBy(() -> {
            //when
            communityValidationService.checkPreviousExistsCommunityName(anyString());
        }).isInstanceOf(AlreadyExistsCommunityNameException.class);
    }

    @Test
    void 존재하지_않는_커뮤니티_조회() {
        //given
        Long notExistsId = Long.MAX_VALUE;

        //then
        assertThatThrownBy(() -> {
            //when
            communityValidationService.checkExistsCommunity(notExistsId);
        }).isInstanceOf(EntityNotFoundException.class);
    }
}