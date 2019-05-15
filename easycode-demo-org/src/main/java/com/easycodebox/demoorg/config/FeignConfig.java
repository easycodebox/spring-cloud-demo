package com.easycodebox.demoorg.config;

import com.easycodebox.spring.cloud.oauth2.DefaultOAuth2FeignRequestInterceptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;

/**
 * {@code @EnableFeignClients} basePackages属性可以使用通配符：com.easycodebox.*.api
 * <p>
 * {@code @EnableFeignClients} basePackages为空时，默认扫描包路径和{@code @SpringBootApplication}一样
 *
 * @author WangXiaoJin
 * @date 2019-04-18 13:21
 */
@Configuration
@EnableCircuitBreaker
@EnableFeignClients(value = "com.easycodebox.demo.api")
public class FeignConfig {

    /**
     * 获取Token传递给下游服务，所有FeignClient共用
     *
     * @param oAuth2ClientContext OAuth2上下文环境
     * @param resource OAuth2ProtectedResourceDetails
     * @return DefaultOAuth2FeignRequestInterceptor
     */
    @Bean
    public DefaultOAuth2FeignRequestInterceptor defaultOAuth2FeignRequestInterceptor(
        ObjectProvider<OAuth2ClientContext> oAuth2ClientContext,
        ObjectProvider<OAuth2ProtectedResourceDetails> resource) {
        return new DefaultOAuth2FeignRequestInterceptor(oAuth2ClientContext.getIfAvailable(),
            resource.getIfAvailable());
    }

}
