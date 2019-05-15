package com.easycodebox.zuul.config;

import com.easycodebox.spring.cloud.oauth2.CheckOAuth2AccessTokenFilter;
import com.easycodebox.spring.cloud.oauth2.OAuth2ExceptionResolver;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.DispatcherType;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.web.filter.RequestContextFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author WangXiaoJin
 * @date 2019-04-13 13:09
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private MessageSource messageSource;

    public WebMvcConfig(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public void extendHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {
        // 获取 OAuth2Exception 的 HttpErrorCode，并设置到 Response 中
        OAuth2ExceptionResolver resolver = new OAuth2ExceptionResolver();
        resolver.setMessageSource(messageSource);
        resolvers.add(resolver);
    }

    /**
     * {@link RequestContextFilter}默认DispatcherType为REQUEST，SpringSecurity默认配置的dispatcherTypes为ASYNC/ERROR/REQUEST。
     * 所以当 DispatcherType为ERROR时，是不会进到 RequestContextFilter里面的，如果此时 SpringSecurity 的 FilterChain中
     * 有Filter用到了SessionScope/RequestScope Bean时会报异常的，如{@link CheckOAuth2AccessTokenFilter}用到了{@link OAuth2RestOperations}，
     * {@link OAuth2RestOperations}用到了{@link OAuth2ClientContext}。
     *
     * @param requestContextFilter requestContextFilter
     * @param securityProperties securityProperties
     * @return FilterRegistrationBean
     */
    @Bean
    public FilterRegistrationBean<RequestContextFilter> requestContextFilterRegistration(
        RequestContextFilter requestContextFilter, SecurityProperties securityProperties) {
        FilterRegistrationBean<RequestContextFilter> registration = new FilterRegistrationBean<>(requestContextFilter);
        registration.setOrder(getObjectOrder(requestContextFilter));
        if (securityProperties.getFilter().getDispatcherTypes() != null) {
            // RequestContextFilter 使用SpringSecurity的dispatcherTypes
            EnumSet<DispatcherType> dispatcherTypes = securityProperties.getFilter().getDispatcherTypes().stream()
                .map(type -> DispatcherType.valueOf(type.name()))
                .collect(Collectors.collectingAndThen(Collectors.toSet(), EnumSet::copyOf));
            registration.setDispatcherTypes(dispatcherTypes);
        }
        return registration;
    }


    /**
     * 获取对象的Order值
     *
     * @param value 解析的对象
     * @return 返回 Order值
     */
    private int getObjectOrder(Object value) {
        return new AnnotationAwareOrderComparator() {
            @Override
            public int getOrder(Object obj) {
                return super.getOrder(obj);
            }
        }.getOrder(value);
    }

}
