package com.easycodebox.demoorg.config;

import com.easycodebox.spring.cloud.oauth2.AuthUserAuthenticationConverter;
import com.easycodebox.spring.cloud.oauth2.ClockSkewTokenServices;
import org.springframework.boot.autoconfigure.security.oauth2.resource.JwtAccessTokenConverterConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.UserAuthenticationConverter;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;


/**
 * {@code @EnableWebSecurity(debug = true)} - 打印请求及跳转相关的信息。如果你不想打印信息可省略此注解，因为SpringBoot默认提供。
 * <p>
 * 想自定义HttpSecurity，请实现{@link ResourceServerConfigurerAdapter}接口，没必要继承{@link WebSecurityConfigurerAdapter}。
 *
 * @author WangXiaoJin
 * @date 2019-03-27 15:35
 */
@Configuration
@EnableResourceServer
@EnableWebSecurity(debug = true)
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class SecurityConfig {

    /**
     * 自定义{@link DefaultTokenServices}功能，{@link ClockSkewTokenServices#loadAuthentication(java.lang.String)}
     * 增加了判断AccessToken是否过期时考虑到ClockSkew。
     * <p>
     * <b>{@link ClockSkewTokenServices}只适用于 ResourceServer</b>
     *
     * @param jwtTokenStore jwtTokenStore
     * @return ClockSkewTokenServices
     */
    @Bean
    public ClockSkewTokenServices jwtTokenServices(TokenStore jwtTokenStore) {
        ClockSkewTokenServices services = new ClockSkewTokenServices();
        services.setTokenStore(jwtTokenStore);
        return services;
    }

    /**
     * 配置{@link JwtAccessTokenConverter}内部的{@link UserAuthenticationConverter}为{@link AuthUserAuthenticationConverter}。
     * 用于解析JwtAccessToken返回更详细的{@link Authentication}信息，而不仅仅返回username和权限。
     * <p>
     * 此bean被{@code ResourceServerTokenServicesConfiguration.JwtTokenServicesConfiguration#jwtTokenEnhancer()}引用。
     *
     * @return JwtAccessTokenConverterConfigurer
     */
    @Bean
    public JwtAccessTokenConverterConfigurer userTokenConverterConfig() {
        return converter -> {
            DefaultAccessTokenConverter tokenConverter = new DefaultAccessTokenConverter();
            tokenConverter.setUserTokenConverter(new AuthUserAuthenticationConverter());
            converter.setAccessTokenConverter(tokenConverter);
        };
    }

}
