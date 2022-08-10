package boogi.apiserver.utils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceUnitUtil;

public class PersistenceUtil {

    private final EntityManager em;
    private final PersistenceUnitUtil unitUtil;

    public PersistenceUtil(EntityManager em) {
        this.em = em;
        this.unitUtil = em.getEntityManagerFactory().getPersistenceUnitUtil();
    }

    public boolean isLoaded(Object entity) {
        return unitUtil.isLoaded(entity);
    }

    public void cleanPersistenceContext() {
        em.flush();
        em.clear();
    }
}