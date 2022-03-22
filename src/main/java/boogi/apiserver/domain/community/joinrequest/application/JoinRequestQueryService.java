package boogi.apiserver.domain.community.joinrequest.application;

import boogi.apiserver.domain.community.joinrequest.dao.JoinRequestRepository;
import boogi.apiserver.domain.community.joinrequest.domain.JoinRequest;
import boogi.apiserver.global.error.exception.EntityNotFoundException;
import boogi.apiserver.global.error.exception.InvalidValueException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JoinRequestQueryService {
    private final JoinRequestRepository joinRequestRepository;

    public JoinRequest getJoinRequest(Long joinRequestId) {
        JoinRequest joinRequest = joinRequestRepository.findById(joinRequestId).orElseThrow(InvalidValueException::new);
        if (joinRequest.getCanceledAt() != null) {
            throw new EntityNotFoundException();
        }
        return joinRequest;
    }

    public List<JoinRequest> getAllRequests(Long communityId) {
        return joinRequestRepository.getAllRequests(communityId);
    }
}
