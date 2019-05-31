package com.easycodebox.oauth.config;

import com.easycodebox.spring.cloud.oauth2.AuthUserAuthenticationConverter;
import com.easycodebox.spring.cloud.oauth2.RedisAuthorizationCodeServices;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.boot.autoconfigure.security.oauth2.OAuth2ClientProperties;
import org.springframework.boot.autoconfigure.security.oauth2.authserver.AuthorizationServerProperties;
import org.springframework.boot.autoconfigure.security.oauth2.authserver.AuthorizationServerTokenServicesConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.authserver.OAuth2AuthorizationServerConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.oauth2.config.annotation.builders.ClientDetailsServiceBuilder;
import org.springframework.security.oauth2.config.annotation.builders.InMemoryClientDetailsServiceBuilder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.endpoint.DefaultRedirectResolver;
import org.springframework.security.oauth2.provider.endpoint.RedirectResolver;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

/**
 * 自定义AuthorizationServerConfigurer，不想用自动配置类{@link OAuth2AuthorizationServerConfiguration}。
 * 多数代码拷贝于{@link AuthorizationServerTokenServicesConfiguration} / {@link OAuth2AuthorizationServerConfiguration}。
 *
 * @author WangXiaoJin
 * @date 2019-03-29 20:39
 */
@Configuration
@EnableAuthorizationServer
@EnableConfigurationProperties(AuthorizationServerProperties.class)
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

    private final BaseClientDetails details;

    private final AuthenticationManager authenticationManager;

    private final AuthorizationServerProperties properties;

    private final UserDetailsService userDetailsService;

    private final RedisTemplate<Object, Object> redisTemplate;

    public AuthorizationServerConfig(BaseClientDetails details,
        AuthenticationConfiguration authenticationConfiguration,
        AuthorizationServerProperties properties,
        UserDetailsService userDetailsService,
        RedisTemplate<Object, Object> redisTemplate) throws Exception {
        this.details = details;
        this.authenticationManager = authenticationConfiguration.getAuthenticationManager();
        this.properties = properties;
        this.userDetailsService = userDetailsService;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        ClientDetailsServiceBuilder<InMemoryClientDetailsServiceBuilder>.ClientBuilder builder = clients
            .inMemory().withClient(details.getClientId());

        builder.secret(details.getClientSecret())
            .resourceIds(details.getResourceIds().toArray(new String[0]))
            .authorizedGrantTypes(details.getAuthorizedGrantTypes().toArray(new String[0]))
            .authorities(AuthorityUtils.authorityListToSet(details.getAuthorities()).toArray(new String[0]))
            .scopes(details.getScope().toArray(new String[0]));

        if (details.getAutoApproveScopes() != null) {
            builder.autoApprove(details.getAutoApproveScopes().toArray(new String[0]));
        }
        if (details.getAccessTokenValiditySeconds() != null) {
            builder.accessTokenValiditySeconds(details.getAccessTokenValiditySeconds());
        }
        if (details.getRefreshTokenValiditySeconds() != null) {
            builder.refreshTokenValiditySeconds(details.getRefreshTokenValiditySeconds());
        }
        if (details.getRegisteredRedirectUri() != null) {
            builder.redirectUris(details.getRegisteredRedirectUri().toArray(new String[0]));
        }
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
        endpoints.accessTokenConverter(jwtTokenEnhancer())
            .tokenStore(jwtTokenStore())
            .authorizationCodeServices(redisAuthorizationCodeServices())
            .redirectResolver(redirectResolver())
            // 设置userDetailsService，不然RefreshToken会因为获取不到用户信息，会重新走整个获取AccessToken流程
            .userDetailsService(userDetailsService);
        if (details.getAuthorizedGrantTypes().contains(OAuthGrantType.PASSWORD.getValue())) {
            endpoints.authenticationManager(authenticationManager);
        }
    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) {
        security.passwordEncoder(PasswordEncoderFactories.createDelegatingPasswordEncoder());
        if (properties.getCheckTokenAccess() != null) {
            security.checkTokenAccess(properties.getCheckTokenAccess());
        }
        if (properties.getTokenKeyAccess() != null) {
            security.tokenKeyAccess(properties.getTokenKeyAccess());
        }
        if (properties.getRealm() != null) {
            security.realm(properties.getRealm());
        }
    }

    /**
     * 自定义RedirectResolver，自动配置的对象缺乏配置入口
     *
     * @return RedirectResolver
     */
    @Bean
    @ConfigurationProperties(prefix = "security.oauth2.redirect-resolver")
    public RedirectResolver redirectResolver() {
        return new DefaultRedirectResolver();
    }

    @Bean
    public TokenStore jwtTokenStore() {
        return new JwtTokenStore(jwtTokenEnhancer());
    }

    @Bean
    public JwtAccessTokenConverter jwtTokenEnhancer() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();

        // 自定义 UserAuthenticationConverter，用于扩展用户信息
        DefaultAccessTokenConverter tokenConverter = new DefaultAccessTokenConverter();
        tokenConverter.setUserTokenConverter(new AuthUserAuthenticationConverter());
        converter.setAccessTokenConverter(tokenConverter);

        converter.setSigningKey(properties.getJwt().getKeyValue());
        // 借用key-alias存储公钥，用于client获取或者AuthServer启用ResourceServer功能
        converter.setVerifierKey(properties.getJwt().getKeyAlias());
        return converter;
    }

    /**
     * 使用Redis存贮AuthorizationCode
     *
     * @return AuthorizationCodeServices
     */
    @Bean
    public AuthorizationCodeServices redisAuthorizationCodeServices() {
        return new RedisAuthorizationCodeServices(redisTemplate);
    }

    @Configuration
    protected static class BaseClientDetailsConfiguration {

        private final OAuth2ClientProperties client;

        protected BaseClientDetailsConfiguration(OAuth2ClientProperties client) {
            this.client = client;
        }

        @Bean
        @ConfigurationProperties(prefix = "security.oauth2.client")
        public BaseClientDetails oauth2ClientDetails() {
            BaseClientDetails details = new BaseClientDetails();
            if (client.getClientId() == null) {
                client.setClientId(UUID.randomUUID().toString());
            }
            details.setClientId(client.getClientId());
            details.setClientSecret(client.getClientSecret());

            List<String> allTypes = Arrays.stream(OAuthGrantType.values())
                .map(OAuthGrantType::getValue)
                .collect(Collectors.toList());
            details.setAuthorizedGrantTypes(allTypes);

            details.setAuthorities(
                AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_USER"));
            details.setRegisteredRedirectUri(Collections.emptySet());
            return details;
        }

    }

    /**
     * OAuth2授权类型
     */
    enum OAuthGrantType {
        /**
         * 授权码类型
         */
        AUTHORIZATION_CODE("authorization_code"),
        /**
         * 资源用户密码类型
         */
        PASSWORD("password"),
        /**
         * 客户端验证类型
         */
        CLIENT_CREDENTIALS("client_credentials"),
        /**
         * 隐式授权类型
         */
        IMPLICIT("implicit"),
        /**
         * 刷新token类型
         */
        REFRESH_TOKEN("refresh_token");

        OAuthGrantType(String value) {
            this.value = value;
        }

        private String value;

        public String getValue() {
            return value;
        }
    }
}
