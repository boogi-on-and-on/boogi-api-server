package boogi.apiserver.utils;

import com.google.common.base.CaseFormat;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import javax.persistence.metamodel.EntityType;
import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;


@Component
public class DataBaseInitializer implements InitializingBean {

    @PersistenceContext
    private EntityManager em;

    private List<EntityInfo> entityInfos;

    @Override
    public void afterPropertiesSet() {
        this.entityInfos = em.getMetamodel()
                .getEntities().stream()
                .map(EntityType::getJavaType)
                .filter(e -> e.isAnnotationPresent(Entity.class))
                .map(EntityInfo::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void clear() {
        em.flush();
        em.createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE").executeUpdate();

        for (EntityInfo entityInfo : entityInfos) {
            String tableName = entityInfo.getTableName();
            String idName = entityInfo.getIdName();

            em.createNativeQuery("TRUNCATE TABLE " + tableName).executeUpdate();
            em.createNativeQuery("ALTER TABLE " + tableName + " ALTER COLUMN " + idName + " RESTART WITH 1")
                    .executeUpdate();
        }

        em.createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE").executeUpdate();
    }

    @Transactional
    public void setup() {

    }


    private static class EntityInfo {
        private String tableName;
        private String idName;

        public EntityInfo(Class<?> entityType) {
            this.tableName = getTableName(entityType);
            this.idName = getIdName(entityType);
        }

        private String getTableName(Class<?> entityType) {
            Table table = entityType.getAnnotation(Table.class);
            if (table != null) {
                return table.name();
            }
            return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, entityType.getSimpleName());
        }

        private String getIdName(Class<?> entityType) {
            if (entityType.isAnnotationPresent(Table.class)) {
                try {
                    Field idField = entityType.getDeclaredField("id");
                    if (idField.isAnnotationPresent(Column.class)) {
                        return idField.getAnnotation(Column.class).name();
                    }
                } catch (NoSuchFieldException e) {
                    throw new RuntimeException(e);
                }
            }
            return this.tableName + "_id";
        }

        public String getTableName() {
            return tableName;
        }

        public String getIdName() {
            return idName;
        }
    }
}
