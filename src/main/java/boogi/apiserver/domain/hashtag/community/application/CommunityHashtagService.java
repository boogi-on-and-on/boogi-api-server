package boogi.apiserver.domain.hashtag.community.application;

import boogi.apiserver.domain.community.community.application.CommunityQueryService;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.hashtag.community.dao.CommunityHashtagRepository;
import boogi.apiserver.domain.hashtag.community.domain.CommunityHashtag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityHashtagService {

    private final CommunityHashtagRepository communityHashtagRepository;

    private final CommunityQueryService communityQueryService;

    @Transactional
    public List<CommunityHashtag> addTags(Long CommunityId, List<String> tags) {
        if (tags == null || tags.size() == 0) {
            return null;
        }

        Community community = communityQueryService.getCommunity(CommunityId);

        List<CommunityHashtag> communityHashtags = tags.stream()
                .map(ht -> CommunityHashtag.of(ht, community))
                .collect(Collectors.toList());

        return communityHashtagRepository.saveAll(communityHashtags);
    }
}
