package boogi.apiserver.annotations;


import boogi.apiserver.global.configuration.QuerydslConfig;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Repository 테스트 작성시 DataJpaTest 대신 사용
 * DataJpaTest시 QuerydslConfig 적용 후 테스트 진행
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)

@DataJpaTest
@Import({QuerydslConfig.class})
public @interface CustomDataJpaTest {
}
