package com.easycodebox.spring.cloud.oauth2;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.lang.Nullable;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

/**
 * 获取 OAuth2Exception 的 HttpErrorCode，并设置到 Response 中，否则客户端始终显示 500 异常，并不会显示 403 等异常。
 *
 * @author WangXiaoJin
 * @date 2019-04-15 9:21
 */
public class OAuth2ExceptionResolver implements HandlerExceptionResolver, MessageSourceAware {

    private static final Logger log = LoggerFactory.getLogger(OAuth2ExceptionResolver.class);

    @Nullable
    private MessageSource messageSource;

    @Override
    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler,
        Exception ex) {
        if (ex instanceof OAuth2Exception) {
            int code = ((OAuth2Exception) ex).getHttpErrorCode();
            String msg = ex.getMessage();
            try {
                if (StringUtils.hasLength(msg)) {
                    String resolvedMsg = (messageSource != null ?
                        messageSource.getMessage(msg, null, msg, LocaleContextHolder.getLocale()) : msg);
                    response.sendError(code, resolvedMsg);
                } else {
                    response.sendError(code);
                }
                return new ModelAndView();
            } catch (IOException e) {
                log.warn("Failure while trying to resolve exception [{}]", ex.getClass().getName(), e);
            }
        }
        return null;
    }
}
