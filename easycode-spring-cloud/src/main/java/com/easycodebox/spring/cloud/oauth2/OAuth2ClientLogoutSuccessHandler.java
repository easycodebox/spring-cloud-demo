package com.easycodebox.spring.cloud.oauth2;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AbstractAuthenticationTargetUrlRequestHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * 此{@link LogoutSuccessHandler}用于OAuth2Client，当OAuth2Client执行logout成功后，302到OAuth2Server 执行 logout，
 * 并传递targetUrl参数，此参数为OAuth2Server logout成功后的跳转地址。
 * targetUrl参数值的获取来源：
 * <ul>
 * <li>OAuth2Client logout http 的 targetUrlParameter</li>
 * <li>OAuth2Client logout http 的 Referer header</li>
 * </ul>
 *
 * @author WangXiaoJin
 * @date 2019-06-04 13:29
 * @see AbstractAuthenticationTargetUrlRequestHandler#determineTargetUrl(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
 */
public class OAuth2ClientLogoutSuccessHandler extends AbstractAuthenticationTargetUrlRequestHandler implements
    LogoutSuccessHandler, InitializingBean {

    public static final String DEFAULT_TARGET_URL_PARAMETER_NAME = "targetUrl";

    /**
     * OAuth2Server Logout Url
     */
    private String serverLogoutUrl;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
        throws IOException, ServletException {
        super.handle(request, response, authentication);
    }

    @Override
    protected String determineTargetUrl(HttpServletRequest request,
        HttpServletResponse response) {
        String targetUrl = super.determineTargetUrl(request, response);
        if (StringUtils.isEmpty(targetUrl)) {
            return serverLogoutUrl;
        }
        if (!UrlUtils.isValidRedirectUrl(targetUrl)) {
            throw new IllegalArgumentException("TargetUrl is not valid redirect url. url: " + targetUrl);
        }

        StringBuilder url = new StringBuilder(serverLogoutUrl);
        if (serverLogoutUrl.indexOf('?') < 0) {
            url.append("?");
        }
        url.append(getServerTargetUrlParameter()).append("=");

        if (!UrlUtils.isAbsoluteUrl(targetUrl)) {
            // targetUrl不是绝对路径，组装成绝对路径
            String baseUrl = UrlUtils
                .buildFullRequestUrl(request.getScheme(), request.getServerName(), request.getServerPort(),
                    request.getContextPath(), null);
            url.append(baseUrl);
        }
        url.append(targetUrl);
        return url.toString();
    }

    public String getServerLogoutUrl() {
        return serverLogoutUrl;
    }

    public void setServerLogoutUrl(String serverLogoutUrl) {
        this.serverLogoutUrl = serverLogoutUrl;
    }

    @Override
    public void afterPropertiesSet() {
        Assert.hasText(serverLogoutUrl, "serverLogoutUrl cannot be null or empty.");
        Assert.isTrue(UrlUtils.isAbsoluteUrl(serverLogoutUrl), "serverLogoutUrl must be absolute url.");
    }

    /**
     * 获取OAuth2Server端的TargetUrlParameter，和{@link #getTargetUrlParameter}一致，
     * 如果{@link #getTargetUrlParameter}没值则使用{@link #DEFAULT_TARGET_URL_PARAMETER_NAME}
     *
     * @return ServerTargetUrlParameter
     */
    private String getServerTargetUrlParameter() {
        return super.getTargetUrlParameter() == null ? DEFAULT_TARGET_URL_PARAMETER_NAME
            : super.getTargetUrlParameter();
    }
}
