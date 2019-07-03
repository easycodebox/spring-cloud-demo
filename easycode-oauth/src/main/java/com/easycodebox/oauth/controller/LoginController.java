package com.easycodebox.oauth.controller;

import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.WebAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.context.support.WebApplicationObjectSupport;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author WangXiaoJin
 * @date 2019-06-06 10:49
 */
@Controller
public class LoginController extends WebApplicationObjectSupport {

    /**
     * 显示登录页面
     *
     * @param error 当前登录请求是否包含错误信息
     * @param request 请求对象
     * @return 返回登录视图
     */
    @GetMapping("/login")
    public ModelAndView login(String error, HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView("/login");
        boolean isError = error != null;
        if (isError) {
            String errorMsg = Objects.requireNonNull(getMessageSourceAccessor())
                .getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials");
            HttpSession session = request.getSession(false);
            if (session != null) {
                // 从Session中获取异常信息
                AuthenticationException ex = (AuthenticationException) session
                    .getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
                if (ex != null) {
                    errorMsg = ex.getMessage();
                }
            }
            modelAndView.addObject("errorMsg", errorMsg);
        }
        return modelAndView;
    }

}
