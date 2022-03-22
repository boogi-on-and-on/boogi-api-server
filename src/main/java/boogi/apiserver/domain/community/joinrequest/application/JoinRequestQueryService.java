package boogi.apiserver.domain.community.joinrequest.application;

import boogi.apiserver.domain.community.joinrequest.dao.JoinRequestRepository;
import boogi.apiserver.domain.community.joinrequest.domain.JoinRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JoinRequestQueryService {
    private final JoinRequestRepository joinRequestRepository;

    public List<JoinRequest> getAllRequests(Long communityId) {
        return joinRequestRepository.getAllRequests(communityId);
    }
}
