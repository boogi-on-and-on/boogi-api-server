package boogi.apiserver.domain.community.community.application;

import boogi.apiserver.domain.community.community.dao.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.community.community.exception.AlreadyExistsCommunityNameException;
import boogi.apiserver.global.error.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityValidationService {

    private final CommunityRepository communityRepository;

    public void checkPreviousExistsCommunityName(String name) {
        communityRepository.findByCommunityNameEquals(name).ifPresent(c -> {
            throw new AlreadyExistsCommunityNameException();
        });
    }

    public Community checkExistsCommunity(Long communityId) {
        return communityRepository.findById(communityId)
                .orElseThrow(EntityNotFoundException::new);
    }

    public boolean checkOnlyPrivateCommunity(Long communityId) {
        Community findCommunity = checkExistsCommunity(communityId);
        return findCommunity.isPrivate() ? true : false;
    }
}
