package com.easycodebox.spring.cloud.oauth2;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.client.filter.OAuth2AuthenticationFailureEvent;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetailsSource;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.util.Assert;
import org.springframework.web.filter.GenericFilterBean;

/**
 * 校验 OAuth2AccessToken 是否已过期，过期则请求Auth Server生成新的Token
 *
 * @author WangXiaoJin
 * @date 2019-04-20 11:37
 */
public class CheckOAuth2AccessTokenFilter extends GenericFilterBean {

    private OAuth2RestOperations restTemplate;

    private ResourceServerTokenServices tokenServices;

    private AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource = new OAuth2AuthenticationDetailsSource();

    private AuthenticationTrustResolver trustResolver = new AuthenticationTrustResolverImpl();

    private ApplicationEventPublisher eventPublisher;

    @Override
    protected void initFilterBean() throws ServletException {
        super.initFilterBean();
        Assert.notNull(restTemplate, "restTemplate must be specified");
        Assert.notNull(tokenServices, "tokenServices must be specified");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && !trustResolver.isAnonymous(authentication)
            && authentication.getDetails() instanceof OAuth2AuthenticationDetails) {
            checkAccessToken((HttpServletRequest) request, authentication);
        }
        chain.doFilter(request, response);
    }

    private void checkAccessToken(HttpServletRequest request, Authentication authentication) {
        OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails) authentication.getDetails();
        OAuth2AccessToken accessToken;
        try {
            accessToken = restTemplate.getAccessToken();
        } catch (OAuth2Exception e) {
            BadCredentialsException bad = new BadCredentialsException("Could not obtain access token", e);
            publish(new OAuth2AuthenticationFailureEvent(bad));
            throw bad;
        }
        if (!accessToken.getValue().equals(details.getTokenValue())) {
            // Token有更新
            try {
                OAuth2Authentication result = tokenServices.loadAuthentication(accessToken.getValue());
                if (authenticationDetailsSource != null) {
                    request.setAttribute(OAuth2AuthenticationDetails.ACCESS_TOKEN_VALUE, accessToken.getValue());
                    request.setAttribute(OAuth2AuthenticationDetails.ACCESS_TOKEN_TYPE, accessToken.getTokenType());
                    result.setDetails(authenticationDetailsSource.buildDetails(request));
                }
                publish(new AuthenticationSuccessEvent(result));
                // 更新 OAuth2Authentication
                SecurityContextHolder.getContext().setAuthentication(result);
            } catch (InvalidTokenException e) {
                BadCredentialsException bad = new BadCredentialsException("Could not obtain user details from token",
                    e);
                publish(new OAuth2AuthenticationFailureEvent(bad));
                throw bad;
            }
        }
    }

    private void publish(ApplicationEvent event) {
        if (eventPublisher != null) {
            eventPublisher.publishEvent(event);
        }
    }

    public void setRestTemplate(OAuth2RestOperations restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void setTokenServices(ResourceServerTokenServices tokenServices) {
        this.tokenServices = tokenServices;
    }

    public void setAuthenticationDetailsSource(
        AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource) {
        this.authenticationDetailsSource = authenticationDetailsSource;
    }

    public void setTrustResolver(AuthenticationTrustResolver trustResolver) {
        this.trustResolver = trustResolver;
    }

    public void setEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }
}
