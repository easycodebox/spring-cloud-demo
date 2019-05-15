package com.easycodebox.spring.cloud.oauth2;

import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.security.oauth2.client.feign.OAuth2FeignRequestInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;

/**
 * 扩展{@link OAuth2FeignRequestInterceptor}功能，原先只能通过{@link OAuth2ClientContext}获取{@link OAuth2AccessToken}，
 * 并没有使用已认证的Token - {@code ((OAuth2AuthenticationDetails) SecurityContextHolder.getContext().getAuthentication().getDetails()).getTokenValue()}。
 * <p>
 * 从{@link OAuth2ClientContext}获取{@link OAuth2AccessToken}意味着你需要提供{@link OAuth2ProtectedResourceDetails}具体信息，
 * 以致其能从Auth Server中获取OAuth2AccessToken，而且始终不会使用上游服务传递过来的Token。
 *
 * @author WangXiaoJin
 * @date 2019-04-19 11:07
 */
@Slf4j
public class DefaultOAuth2FeignRequestInterceptor extends OAuth2FeignRequestInterceptor {

    /**
     * 标记是否提供了{@link OAuth2ClientContext}
     */
    private final boolean hasClientContext;

    private final String tokenType;

    private final String header;

    public DefaultOAuth2FeignRequestInterceptor(
        OAuth2ClientContext oAuth2ClientContext,
        OAuth2ProtectedResourceDetails resource) {
        this(oAuth2ClientContext, resource, BEARER, AUTHORIZATION);
    }

    public DefaultOAuth2FeignRequestInterceptor(OAuth2ClientContext oAuth2ClientContext,
        OAuth2ProtectedResourceDetails resource, String tokenType, String header) {
        super(oAuth2ClientContext, resource, tokenType, header);
        hasClientContext = oAuth2ClientContext != null;
        this.tokenType = tokenType;
        this.header = header;
    }

    @Override
    public void apply(RequestTemplate template) {
        if (template.headers().containsKey(header)) {
            log.debug("{} request already has {} header.", template.url(), header);
            return;
        }
        String token = extract(tokenType);
        if (token != null) {
            template.header(header, extract(tokenType));
            log.debug("{} request add token header {}: {}", template.url(), header, token);
        }
    }

    @Override
    protected String extract(String defaultTokenType) {
        String tokenValue = null;
        String type = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getDetails() instanceof OAuth2AuthenticationDetails) {
            // 从SecurityContext中获取Token
            OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails) authentication.getDetails();
            tokenValue = details.getTokenValue();
            type = details.getTokenType();
        } else if (hasClientContext) {
            // 提供了 OAuth2ClientContext ，则可以通过 OAuth2ClientContext 获取 OAuth2AccessToken
            OAuth2AccessToken token = getToken();
            tokenValue = token.getValue();
            type = token.getTokenType();
        }
        if (type == null) {
            type = defaultTokenType;
        }
        return tokenValue == null ? null : String.format("%s %s", type, tokenValue);
    }

}
