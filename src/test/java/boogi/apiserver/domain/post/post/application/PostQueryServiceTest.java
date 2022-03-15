package boogi.apiserver.domain.post.post.application;

import boogi.apiserver.domain.post.post.dao.PostRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostQueryServiceTest {

    @Mock
    PostRepository postRepository;

    @InjectMocks
    PostQueryService postQueryService;

    //todo: Page 객체 직접 생성해서 테스트하기

}