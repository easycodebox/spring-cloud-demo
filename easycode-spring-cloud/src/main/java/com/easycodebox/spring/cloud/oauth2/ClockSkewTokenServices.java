package com.easycodebox.spring.cloud.oauth2;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationManager;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationProcessingFilter;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;

/**
 * 注：<b>此类只适用于 Resource Server</b>，用于 {@link OAuth2AuthenticationProcessingFilter} 的 {@code authenticationManager.authenticate(authentication)}
 * <p>
 * {@link OAuth2AuthenticationManager#authenticate(org.springframework.security.core.Authentication)} 用到了
 * {@link ClockSkewTokenServices#loadAuthentication(java.lang.String)} 方法，此方法中会判断 OAuth2AccessToken 有没有过期，
 * 过期则抛{@link InvalidTokenException} 异常。
 * <p>
 * <b>因各个服务器的系统时间会有偏差，导致 OAuth2AccessToken 在整个调用链路中会出现提前过期的现象。</b> 所以在
 * {@link ClockSkewTokenServices#loadAuthentication(java.lang.String)}方法中增加了容忍时间偏差功能。此功能同样会尽可能
 * 确保调用链路中不会出现Token过期异常，要出现也只能在源头出报出此异常。
 *
 * <ul>
 * <li>此类为<b>过度类</b>，在SpringOAuth2没提供类似功能时使用，如果SpringOAuth2已提供了类似功能，则建议使用官方功能。</li>
 * <li>
 * <b>spring-security-oauth2-resource-server</b>模块提供了类似功能{@code JwtTimestampValidator}，后面有时间的话
 * 验证 ResourceServer 依赖模块能否从 spring-cloud-starter-oauth2 切换至 spring-boot-starter-oauth2-resource-server，
 * 使用 {@code JwtTimestampValidator} 功能
 * </li>
 * </ul>
 *
 * @author WangXiaoJin
 * @date 2019-04-22 18:50
 */
public class ClockSkewTokenServices extends DefaultTokenServices {

    private TokenStore tokenStore;

    private ClientDetailsService clientDetailsService;

    /**
     * 各服务系统时间允许最大的时间偏差，默认：60s
     */
    private int maxClockSkew = 60 * 1000;

    /**
     * Load the credentials for the specified access token.
     *
     * @param accessTokenValue The access token value.
     * @return The authentication for the access token.
     * @throws AuthenticationException If the access token is expired
     * @throws InvalidTokenException if the token isn't valid
     */
    @Override
    public OAuth2Authentication loadAuthentication(String accessTokenValue) {
        OAuth2AccessToken accessToken = tokenStore.readAccessToken(accessTokenValue);
        if (accessToken == null) {
            throw new InvalidTokenException("Invalid access token: " + accessTokenValue);
        } else if (isExpiredByClockSkew(accessToken)) {
            tokenStore.removeAccessToken(accessToken);
            throw new InvalidTokenException("Access token expired: " + accessTokenValue);
        }

        OAuth2Authentication result = tokenStore.readAuthentication(accessToken);
        if (result == null) {
            // in case of race condition
            throw new InvalidTokenException("Invalid access token: " + accessTokenValue);
        }
        if (clientDetailsService != null) {
            String clientId = result.getOAuth2Request().getClientId();
            try {
                clientDetailsService.loadClientByClientId(clientId);
            } catch (ClientRegistrationException e) {
                throw new InvalidTokenException("Client not valid: " + clientId, e);
            }
        }
        return result;
    }

    /**
     * 判断 accessToken 是否过期时考虑 {@link #maxClockSkew}
     *
     * @param accessToken OAuth2AccessToken
     * @return OAuth2AccessToken是否过期
     */
    private boolean isExpiredByClockSkew(OAuth2AccessToken accessToken) {
        if (accessToken.getExpiration() == null) {
            return false;
        }
        long exp = accessToken.getExpiration().getTime() + maxClockSkew;
        return exp < System.currentTimeMillis();
    }

    @Override
    public void setTokenStore(TokenStore tokenStore) {
        super.setTokenStore(tokenStore);
        this.tokenStore = tokenStore;
    }

    @Override
    public void setClientDetailsService(ClientDetailsService clientDetailsService) {
        super.setClientDetailsService(clientDetailsService);
        this.clientDetailsService = clientDetailsService;
    }

    public void setMaxClockSkew(int maxClockSkew) {
        this.maxClockSkew = maxClockSkew;
    }
}
