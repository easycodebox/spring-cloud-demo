package com.easycodebox.spring.cloud.feign;

import feign.FeignException;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

/**
 * 获取 FeignException 的Http Code，并设置到 Response 中，否则客户端始终显示 500 异常，并不会显示 403 等异常。
 *
 * @author WangXiaoJin
 * @date 2019-04-15 9:21
 */
public class FeignExceptionResolver implements HandlerExceptionResolver {

    private static final Logger log = LoggerFactory.getLogger(FeignExceptionResolver.class);

    public static final String FEIGN_EXCEPTION_CONTENT_KEY = "javax.servlet.error.feign_exception_content";

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler,
        Exception ex) {
        FeignException feignExc = null;
        String msg = null;
        if (ex instanceof FeignException) {
            feignExc = (FeignException) ex;
            msg = ex.getMessage();
        } else if (ex.getCause() instanceof FeignException) {
            feignExc = (FeignException) ex.getCause();
            msg = ex.getMessage() + feignExc.getMessage();
        }
        if (feignExc != null) {
            try {
                // 保存Feign调用远程服务时返回的数据
                request.setAttribute(FEIGN_EXCEPTION_CONTENT_KEY, feignExc.contentUTF8());

                if (StringUtils.hasLength(msg)) {
                    response.sendError(feignExc.status(), msg);
                } else {
                    response.sendError(feignExc.status());
                }
                return new ModelAndView();
            } catch (IOException e) {
                log.warn("Failure while trying to resolve exception [{}]", ex.getClass().getName(), e);
            }
        }
        return null;
    }
}
