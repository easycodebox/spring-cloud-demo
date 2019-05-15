package com.easycodebox.zuul.config;

import com.easycodebox.spring.cloud.oauth2.AuthUserAuthenticationConverter;
import com.easycodebox.spring.cloud.oauth2.CheckOAuth2AccessTokenFilter;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.boot.autoconfigure.security.oauth2.resource.JwtAccessTokenConverterConfigurer;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoRestTemplateCustomizer;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoRestTemplateFactory;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.cloud.security.oauth2.proxy.OAuth2ProxyAutoConfiguration;
import org.springframework.cloud.security.oauth2.proxy.OAuth2TokenRelayFilter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.security.oauth2.provider.token.UserAuthenticationConverter;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

/**
 * 当自定义{@link WebSecurityConfigurerAdapter}类时，{@code @EnableOAuth2Sso}最好与此类一起，如下。
 * <p>
 * 如果{@code @EnableOAuth2Sso}注在Main类上时，会自动重复创建一个{@link WebSecurityConfigurerAdapter}对象{@code OAuth2SsoDefaultConfiguration}
 * <p>
 * {@code @EnableWebSecurity(debug = true)} - 打印请求及跳转相关的信息。如果你不想打印信息可省略此注解，因为SpringBoot默认提供。
 *
 * @author WangXiaoJin
 * @date 2019-03-27 15:35
 */
@Configuration
@EnableOAuth2Sso
@EnableWebSecurity(debug = true)
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private ErrorProperties errorProperties;

    public WebSecurityConfig(ServerProperties serverProperties) {
        this.errorProperties = serverProperties.getError();
    }

    @Override
    public void configure(WebSecurity web) {
        web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 创建 CheckOAuth2AccessTokenFilter ，用于更新 OAuth2 Token
        ApplicationContext context = http.getSharedObject(ApplicationContext.class);
        OAuth2RestOperations restTemplate = context.getBean(UserInfoRestTemplateFactory.class)
            .getUserInfoRestTemplate();
        ResourceServerTokenServices tokenServices = context.getBean(ResourceServerTokenServices.class);
        CheckOAuth2AccessTokenFilter checkAccessTokenFilter = new CheckOAuth2AccessTokenFilter();
        checkAccessTokenFilter.setRestTemplate(restTemplate);
        checkAccessTokenFilter.setTokenServices(tokenServices);
        checkAccessTokenFilter.setEventPublisher(context);

        http.requestMatchers().anyRequest()
            .and().authorizeRequests()
            .antMatchers(errorProperties.getPath()).permitAll()
            .anyRequest().authenticated()
            .and().logout().permitAll()
            .and().addFilterAfter(checkAccessTokenFilter, ExceptionTranslationFilter.class);
    }

    /**
     * 扩展容器中的OAuth2ClientContextFilter对象，验证当前有没有缓存请求，没有则缓存当前请求。
     * <p>
     * 扩展原因：当抛出UserRedirectRequiredException异常时，OAuth2ClientContextFilter会跳转至授权页面。
     * 如果此异常是由程序中的OAuth2RestTemplate触发，则不会缓存当前请求，导致授权成功后，跳转至根路径(/)。
     * ExceptionTranslationFilter只缓存抛出AuthenticationException、AccessDeniedException异常的请求。
     *
     * @return BeanPostProcessor
     */
    @Bean
    public static BeanPostProcessor auth2ClientContextFilterBeanPostProcessor() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) {
                if (bean instanceof OAuth2ClientContextFilter) {
                    ((OAuth2ClientContextFilter) bean).setRedirectStrategy(new DefaultRedirectStrategy() {

                        HttpSessionRequestCache requestCache = new HttpSessionRequestCache();

                        @Override
                        public void sendRedirect(HttpServletRequest request, HttpServletResponse response, String url)
                            throws IOException {
                            // 抛出UserRedirectRequiredException异常执行跳转时，验证当前有没有缓存请求，没有则缓存当前请求
                            SavedRequest savedRequest = requestCache.getRequest(request, response);
                            if (savedRequest == null) {
                                requestCache.saveRequest(request, response);
                            }
                            super.sendRedirect(request, response, url);
                        }
                    });
                }
                return bean;
            }
        };
    }

    /**
     * 设置{@code OAuth2RestTemplate.retryBadAccessTokens = false}，不然 Resource Server 返回 UserDeniedAuthorizationException 时，
     * OAuth2ErrorHandler 会转换成 OAuth2AccessDeniedException。此时 OAuth2RestTemplate 捕获 OAuth2AccessDeniedException异常，
     * 并清空当前 AccessToken，再次执行逻辑并重新获取 AccessToken，如果此时授权方式为Code时且授权成功后，你会发现永远显示
     * 不了403页面。因为这不是因为AccessToken无效导致访问拒绝，而是因为Resource Server禁止Client防止特定的资源返回的403.
     *
     * @return UserInfoRestTemplateCustomizer
     */
    @Bean
    public UserInfoRestTemplateCustomizer templateCustomizer() {
        return template -> template.setRetryBadAccessTokens(false);
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

    /**
     * 暴露{@link OAuth2RestTemplate} bean
     *
     * @param templateFactory templateFactory
     * @return OAuth2RestTemplate
     */
    @Bean
    public OAuth2RestTemplate auth2RestTemplate(UserInfoRestTemplateFactory templateFactory) {
        return templateFactory.getUserInfoRestTemplate();
    }

    /**
     * {@link OAuth2TokenRelayFilter}会用到{@link OAuth2RestTemplate}，但对它来说没多大意义。
     * 因为当使用authorization_code模式时，OAuth2RestTemplate获取AccessToken可能会报UserRedirectRequiredException异常。
     * 而此异常会被 OAuth2TokenRelayFilter 吃掉，这种现象是有问题的。
     * <p>
     * 所以验证Token有没有过期及获取Token不应该在 OAuth2TokenRelayFilter 里面处理，
     * 唯一可以在OAuth2TokenRelayFilter里面处理的情况是：刷新Token。参考{@link OAuth2ProxyAutoConfiguration#oauth2TokenRelayFilter()}
     * <p>
     * <b>注：刷新/获取Token改用{@link CheckOAuth2AccessTokenFilter}<b/>
     *
     * @return BeanPostProcessor
     */
    @Bean
    public static BeanPostProcessor auth2TokenRelayFilterBeanPostProcessor() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) {
                if (bean instanceof OAuth2TokenRelayFilter) {
                    ((OAuth2TokenRelayFilter) bean).setRestTemplate(null);
                }
                return bean;
            }
        };
    }
}
