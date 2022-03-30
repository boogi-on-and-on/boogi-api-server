package boogi.apiserver.domain.community.community.application;

import boogi.apiserver.domain.community.community.dao.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.member.dao.MemberRepository;
import boogi.apiserver.global.error.exception.EntityNotFoundException;
import boogi.apiserver.global.error.exception.InvalidValueException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CommunityQueryService {
    private final CommunityRepository communityRepository;
    private final MemberRepository memberRepository;


    public Community getCommunity(Long communityId) {
        Community community = communityRepository.findById(communityId).orElseThrow(InvalidValueException::new);
        if (community.getCanceledAt() != null) {
            throw new EntityNotFoundException();
        }

        return community;
    }

    public Community getCommunityWithHashTag(Long communityId) {
        Community community = this.getCommunity(communityId);
        community.getHashtags().size(); //LAZY INIT

        return community;
    }
}
