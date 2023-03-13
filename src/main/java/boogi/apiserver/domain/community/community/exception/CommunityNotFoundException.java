package boogi.apiserver.domain.community.community.exception;

import boogi.apiserver.global.error.exception.EntityNotFoundException;

public class CommunityNotFoundException extends EntityNotFoundException {

    private static final String MESSAGE = "해당 커뮤니티가 존재하지 않습니다.";

    public CommunityNotFoundException() {
        super(MESSAGE);
    }
}
