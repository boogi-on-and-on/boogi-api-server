package boogi.apiserver.domain.community.community.application;

import boogi.apiserver.domain.community.community.dao.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.community.community.exception.AlreadyExistsCommunityNameException;
import boogi.apiserver.global.error.exception.InvalidValueException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityValidationService {

    private final CommunityRepository communityRepository;

    private final CommunityQueryService communityQueryService;

    public void checkPreviousExistsCommunityName(String name) {
        communityRepository.findByCommunityNameEquals(name).ifPresent(c -> {
            throw new AlreadyExistsCommunityNameException();
        });
    }

    public void checkPrivateCommunity(Long communityId) {
        Community findCommunity = communityRepository.findByCommunityId(communityId);
        if (findCommunity.isPrivate()) {
            throw new InvalidValueException("비공개 커뮤니티입니다.");
        }
    }
}
