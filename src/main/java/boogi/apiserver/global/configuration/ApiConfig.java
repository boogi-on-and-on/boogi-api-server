package boogi.apiserver.global.configuration;

import boogi.apiserver.global.argument_resolver.session.SessionArgumentResolver;
import boogi.apiserver.global.converter.StringToBooleanConverter;
import boogi.apiserver.global.interceptor.SessionValidationInterceptor;
import boogi.apiserver.global.webclient.HttpInvocation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.session.web.http.CookieHttpSessionIdResolver;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.session.web.http.HeaderHttpSessionIdResolver;
import org.springframework.session.web.http.HttpSessionIdResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class ApiConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringToBooleanConverter());
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        /**
         * TODO: 회원생성 및 로그인 부분에 excludePathPatterns 적용하기
         */
        registry.addInterceptor(new SessionValidationInterceptor())
                .order(1)
                .addPathPatterns("/**")
                .excludePathPatterns("/api/users/token/**");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new SessionArgumentResolver());
    }

    /**
     * Cookie에 있는 SESSION을 이용하는 방식에서 Header에 있는 X-Auth-Token으로 변경
     * HttpSessionIdResolver 역할
     * 1. X-Auth-Token이라는 HTTP Header안에 있는 토큰으로 sessionId를 찾는다
     * 2. 토큰 발급할 때, X-Auth-Token에 토큰을 준다.
     */
    @Bean
    public HttpSessionIdResolver httpSessionIdResolver() {
        CookieHttpSessionIdResolver resolver = new CookieHttpSessionIdResolver();
        DefaultCookieSerializer cookieSerializer = new DefaultCookieSerializer();
        cookieSerializer.setUseBase64Encoding(false); // 토큰을 encode X
        resolver.setCookieSerializer(cookieSerializer);

        return HeaderHttpSessionIdResolver.xAuthToken();
    }

    @Bean
    public HttpInvocation httpInvocation() {
        return new HttpInvocation();
    }

}
