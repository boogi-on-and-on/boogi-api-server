package boogi.apiserver.global.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

import javax.annotation.PostConstruct;

@EnableRedisHttpSession
@ConditionalOnProperty(name = "spring.session.store-type", havingValue = "redis")
@Slf4j
public class RedisSessionConfig {

    @Value("${spring.session.store-type}")
    private String sessionStoreType;

    @PostConstruct
    public void init() {
        String redisStatus = sessionStoreType.equals("redis") ? "ON" : "OFF";
        log.info("Redis Session Storage is turn {}", redisStatus);
    }

    @Bean
    public RedisSerializer springSessionDefaultRedisSerializer() {
        return new GenericJackson2JsonRedisSerializer();
    }
}