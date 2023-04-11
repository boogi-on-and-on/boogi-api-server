package boogi.apiserver.utils;

import boogi.apiserver.global.configuration.QuerydslConfig;
import boogi.apiserver.global.log.P6SpySqlFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import javax.persistence.EntityManager;

@DataJpaTest
@Import({QuerydslConfig.class, P6SpySqlFormatter.class})
public class RepositoryTest {

    @Autowired
    protected EntityManager entityManager;

    protected void cleanPersistenceContext() {
        entityManager.flush();
        entityManager.clear();
    }

    protected boolean isLoaded(Object entity) {
        return entityManager.getEntityManagerFactory().getPersistenceUnitUtil().isLoaded(entity);
    }
}
