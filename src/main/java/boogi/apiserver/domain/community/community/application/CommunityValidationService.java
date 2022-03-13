package boogi.apiserver.domain.community.community.application;

import boogi.apiserver.domain.community.community.dao.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.community.community.exception.AlreadyExistsCommunityNameException;
import boogi.apiserver.global.error.exception.EntityNotFoundException;
import boogi.apiserver.global.error.exception.InvalidValueException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityValidationService {

    private final CommunityRepository communityRepository;

    public Community getCommunity(Long communityId) {
        Community community = communityRepository.findById(communityId).orElseThrow(InvalidValueException::new);
        if (community.getCanceledAt() != null) {
            throw new EntityNotFoundException();
        }
        return community;
    }

    public void checkPreviousExistsCommunityName(String name) {
        communityRepository.findByCommunityNameEquals(name).ifPresent(c -> {
            throw new AlreadyExistsCommunityNameException();
        });
    }


}
