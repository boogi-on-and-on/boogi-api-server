package boogi.apiserver.utils;

import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.user.domain.User;
import com.google.common.base.CaseFormat;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import javax.persistence.metamodel.EntityType;
import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

import static boogi.apiserver.utils.fixture.CommunityFixture.*;
import static boogi.apiserver.utils.fixture.MemberFixture.*;
import static boogi.apiserver.utils.fixture.UserFixture.*;


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
        User sundo = SUNDO.toUser();
        User yongjin = YONGJIN.toUser();
        User deokhwan = DEOKHWAN.toUser();
        List.of(sundo, yongjin, deokhwan).forEach(user -> em.persist(user));
        em.flush();

        Community baseball = BASEBALL.toCommunity();
        Community pocs = POCS.toCommunity();
        Community english = ENGLISH.toCommunity();
        List.of(baseball, pocs, english).forEach(community -> em.persist(community));
        em.flush();

        List.of(
                SUNDO_POCS.toMember(sundo, pocs),
                YONGJIN_POCS.toMember(yongjin, pocs),
                DEOKHWAN_POCS.toMember(deokhwan, pocs),
                YONGJIN_ENGLISH.toMember(yongjin, english)
        ).forEach(member -> em.persist(member));
        em.flush();
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
